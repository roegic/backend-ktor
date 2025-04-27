package com.example.repository

import com.example.data.model.*
import com.example.data.table.*
import com.example.repository.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction

class Repo {

    suspend fun addUser(user: User) {
        dbQuery {
            UserTable.insert { ut ->
                ut[UserTable.email] = user.email
                ut[UserTable.username] = user.username
                ut[UserTable.hashPassword] = user.hashPassword
            }
        }
    }

    suspend fun getUsersRankedByInterests(userId: Int): List<UserInfo> {
        return dbQuery {
            val currentUserInterestTagIds = UserInterestsTable
                .select(UserInterestsTable.interestTagId)
                .where { UserInterestsTable.userId eq userId }
                .map { it[UserInterestsTable.interestTagId] }

            val allUsers = UserInfoTable
                .join(UserInterestsTable, JoinType.INNER, UserInfoTable.userId, UserInterestsTable.userId)
                .select(
                    UserInfoTable.userId, UserInfoTable.firstName, UserInfoTable.lastName,
                    UserInfoTable.age, UserInfoTable.bio, UserInfoTable.country,
                    UserInfoTable.city, UserInfoTable.occupation, UserInfoTable.photo, UserInfoTable.interests,
                    UserInterestsTable.interestTagId
                )
                .where { UserInfoTable.userId neq userId }

            val rankedUsers = allUsers.groupBy { row ->
                row[UserInfoTable.userId]
            }.map { (userId, rows) ->
                val otherUserInterestTagIds = rows.map { it[UserInterestsTable.interestTagId] }

                val matchingCount = currentUserInterestTagIds.intersect(otherUserInterestTagIds.toSet()).size

                Pair(
                    UserInfo(
                        userId = rows.first()[UserInfoTable.userId],
                        firstName = rows.first()[UserInfoTable.firstName],
                        lastName = rows.first()[UserInfoTable.lastName],
                        age = rows.first()[UserInfoTable.age],
                        bio = rows.first()[UserInfoTable.bio],
                        country = rows.first()[UserInfoTable.country],
                        city = rows.first()[UserInfoTable.city],
                        occupation = rows.first()[UserInfoTable.occupation],
                        photo = rows.first()[UserInfoTable.photo],
                        interests = rows.first()[UserInfoTable.interests]
                    ),
                    matchingCount
                )
            }.filter { it.second >= 0 }

            rankedUsers.sortedWith(
                compareByDescending<Pair<UserInfo, Int>> { it.second }
                    .thenBy { it.first.userId }
            ).map { it.first }
        }
    }

    private fun getUsersRankedByInterestsSync(userId: Int): List<UserInfo> {
        return transaction {
            val currentUserInterestTagIds = UserInterestsTable
                .select(UserInterestsTable.interestTagId)
                .where { UserInterestsTable.userId eq userId }
                .map { it[UserInterestsTable.interestTagId] }

            val allUsers = UserInfoTable
                .join(UserInterestsTable, JoinType.INNER, UserInfoTable.userId, UserInterestsTable.userId)
                .select(
                    UserInfoTable.userId, UserInfoTable.firstName, UserInfoTable.lastName,
                    UserInfoTable.age, UserInfoTable.bio, UserInfoTable.country,
                    UserInfoTable.city, UserInfoTable.occupation, UserInfoTable.photo, UserInfoTable.interests,
                    UserInterestsTable.interestTagId
                )
                .where { UserInfoTable.userId neq userId }

            val rankedUsers = allUsers.groupBy { row ->
                row[UserInfoTable.userId]
            }.map { (userId, rows) ->
                val otherUserInterestTagIds = rows.map { it[UserInterestsTable.interestTagId] }

                val matchingCount = currentUserInterestTagIds.intersect(otherUserInterestTagIds.toSet()).size

                Pair(
                    UserInfo(
                        userId = rows.first()[UserInfoTable.userId],
                        firstName = rows.first()[UserInfoTable.firstName],
                        lastName = rows.first()[UserInfoTable.lastName],
                        age = rows.first()[UserInfoTable.age],
                        bio = rows.first()[UserInfoTable.bio],
                        country = rows.first()[UserInfoTable.country],
                        city = rows.first()[UserInfoTable.city],
                        occupation = rows.first()[UserInfoTable.occupation],
                        photo = rows.first()[UserInfoTable.photo],
                        interests = rows.first()[UserInfoTable.interests]
                    ),
                    matchingCount
                )
            }

            rankedUsers.sortedWith(
                compareByDescending<Pair<UserInfo, Int>> { it.second }
                    .thenBy { it.first.userId }
            ).map { it.first }
        }
    }


    suspend fun getFilteredUsers(
        userId: Int,
        selectedInterests: List<String>?,
        minAge: String?,
        maxAge: String?,
        city: String?,
        country: String?,
        languages: List<String>?
    ): List<UserInfo> = dbQuery {
        if (selectedInterests == null && minAge == null && maxAge == null && city == null && country == null && languages == null) {
            return@dbQuery getUsersRankedByInterestsSync(userId)
        }

        val currentUserInterests = UserInterestsTable
            .selectAll().where { UserInterestsTable.userId eq userId }
            .map { it[UserInterestsTable.interestTagId] }

        var baseQuery = UserInfoTable
            .join(UserInterestsTable, JoinType.INNER, UserInfoTable.userId, UserInterestsTable.userId)
            .join(InterestsTagsTable, JoinType.INNER, UserInterestsTable.interestTagId, InterestsTagsTable.id)
            .join(UserLanguagesTable, JoinType.LEFT, UserInfoTable.userId, UserLanguagesTable.userId)
            .join(LanguagesTable, JoinType.LEFT, UserLanguagesTable.languageId, LanguagesTable.id)
            .select(
                UserInfoTable.userId, UserInfoTable.firstName, UserInfoTable.lastName,
                UserInfoTable.age, UserInfoTable.bio, UserInfoTable.country,
                UserInfoTable.city, UserInfoTable.occupation, UserInfoTable.photo, UserInfoTable.interests,
                UserInterestsTable.interestTagId
            )
            .where { UserInfoTable.userId neq userId }

        minAge?.toIntOrNull()?.let { min ->
            baseQuery = baseQuery.andWhere { UserInfoTable.age greaterEq min }
        }

        maxAge?.toIntOrNull()?.let { max ->
            baseQuery = baseQuery.andWhere { UserInfoTable.age lessEq max }
        }

        if (!city.isNullOrBlank()) {
            baseQuery = baseQuery.andWhere { UserInfoTable.city eq city }
        }

        if (!selectedInterests.isNullOrEmpty()) {
            baseQuery = baseQuery.andWhere { InterestsTagsTable.tag inList selectedInterests }
        }

        if (!country.isNullOrBlank()) {
            baseQuery = baseQuery.andWhere { UserInfoTable.country eq country }
        }

        if (!languages.isNullOrEmpty()) {
            baseQuery = baseQuery.andWhere { LanguagesTable.nativeName inList languages }
        }

        val userInterestMap = mutableMapOf<Int, MutableList<Int>>()

        baseQuery.forEach { row ->
            val otherUserId = row[UserInfoTable.userId]
            val tagId = row[UserInterestsTable.interestTagId]

            val tagList = userInterestMap.getOrPut(otherUserId) { mutableListOf() }
            tagList.add(tagId)
        }

        val usersInfo = userInterestMap.mapNotNull { (otherUserId, tags) ->
            val matchingCount = tags.intersect(currentUserInterests).size
            val row = UserInfoTable
                .selectAll().where { UserInfoTable.userId eq otherUserId }
                .singleOrNull() ?: return@mapNotNull null

            Pair(
                UserInfo(
                    userId = row[UserInfoTable.userId],
                    firstName = row[UserInfoTable.firstName],
                    lastName = row[UserInfoTable.lastName],
                    age = row[UserInfoTable.age],
                    bio = row[UserInfoTable.bio],
                    country = row[UserInfoTable.country],
                    city = row[UserInfoTable.city],
                    occupation = row[UserInfoTable.occupation],
                    photo = row[UserInfoTable.photo],
                    interests = row[UserInfoTable.interests]
                ),
                matchingCount
            )
        }

        usersInfo.sortedWith(
            compareByDescending<Pair<UserInfo, Int>> { it.second }
                .thenBy { it.first.userId }
        ).map { it.first }
    }

    suspend fun findUserIdByEmail(email: String): Int? {
        return dbQuery {
            UserTable
                .selectAll().where { UserTable.email eq email }
                .map { it[UserTable.id] }
                .singleOrNull()
        }
    }

    suspend fun getUserInfo(id: Int): UserInfo? {
        return dbQuery {
            val result = UserInfoTable.selectAll().where { UserInfoTable.userId eq id }
                .singleOrNull()

            result?.let {
                UserInfo(
                    userId = id,
                    firstName = it[UserInfoTable.firstName],
                    lastName = it[UserInfoTable.lastName],
                    age = it[UserInfoTable.age],
                    bio = it[UserInfoTable.bio],
                    country = it[UserInfoTable.country],
                    city = it[UserInfoTable.city],
                    occupation = it[UserInfoTable.occupation],
                    photo = it[UserInfoTable.photo],
                    interests = it[UserInfoTable.interests]
                )
            }
        }
    }

    suspend fun updateUserInfo(userInfo: UserInfo) {
        dbQuery {
            UserInfoTable.update({ UserInfoTable.userId eq userInfo.userId }) {
                it[firstName] = userInfo.firstName
                it[lastName] = userInfo.lastName
                it[age] = userInfo.age
                it[bio] = userInfo.bio
                it[country] = userInfo.country
                it[city] = userInfo.city
                it[occupation] = userInfo.occupation
                it[photo] = userInfo.photo
                it[interests] = userInfo.interests
            }
        }
    }


    suspend fun findUserByEmail(email: String) = dbQuery {
        UserTable.selectAll().where { UserTable.email.eq<String>(email) }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun findUserByUsername(username: String) = dbQuery {
        UserTable.selectAll().where { UserTable.username.eq<String>(username) }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    private fun rowToUser(row: ResultRow?): User? {
        if (row == null) {
            return null
        }
        return User(
            userId = row[UserTable.id],
            email = row[UserTable.email],
            username = row[UserTable.username],
            hashPassword = row[UserTable.hashPassword]
        )
    }

    // USER INFO

    suspend fun addUserInfo(info: UserInfo) {
        dbQuery {
            UserInfoTable.insert { uit ->
                uit[UserInfoTable.userId] = info.userId
                uit[UserInfoTable.firstName] = info.firstName
                uit[UserInfoTable.lastName] = info.lastName
                uit[UserInfoTable.bio] = info.bio
                uit[UserInfoTable.country] = info.country
                uit[UserInfoTable.city] = info.city
                uit[UserInfoTable.occupation] = info.occupation
                uit[UserInfoTable.photo] = info.photo
                uit[UserInfoTable.interests] = info.interests
            }
        }
    }

    // INTERESTS


    suspend fun getUserInterests(userId: Int): List<Interest> = dbQuery {
        UserInterestsTable
            .join(
                InterestsTagsTable,
                JoinType.INNER,
                onColumn = UserInterestsTable.interestTagId,
                otherColumn = InterestsTagsTable.id
            )
            .selectAll().where { UserInterestsTable.userId eq userId }
            .map { row ->
                Interest(
                    name = row[UserInterestsTable.interest],
                    type = row[InterestsTagsTable.tag]
                )
            }
    }


    suspend fun updateUserInterests(userId: Int, interests: List<Interest>) {
        dbQuery {
            UserInterestsTable.deleteWhere { UserInterestsTable.userId eq userId }
            interests.forEach { interest ->
                val tagId = InterestsTagsTable
                    .selectAll().where { InterestsTagsTable.tag eq interest.type }
                    .map { it[InterestsTagsTable.id] }
                    .firstOrNull()

                tagId?.let {
                    UserInterestsTable.insert {
                        it[UserInterestsTable.userId] = userId
                        it[UserInterestsTable.interest] = interest.name
                        it[UserInterestsTable.interestTagId] = tagId
                    }
                }
            }
        }
    }

    suspend fun getAllInterestsTags(): List<String> = dbQuery {
        InterestsTagsTable
            .selectAll()
            .map { it[InterestsTagsTable.tag] }
    }

    // SOCIALS
    suspend fun updateUserSocials(userId: Int, socials: List<Social>) {
        dbQuery {
            UserSocialsTable.deleteWhere { UserSocialsTable.userId eq userId }

            socials.forEach { social ->
                UserSocialsTable.insert {
                    it[UserSocialsTable.userId] = userId
                    it[UserSocialsTable.socialName] = social.socialName
                    it[UserSocialsTable.socialLink] = social.socialLink
                }
            }
        }
    }

    suspend fun getUserSocials(userId: Int): List<Social> {
        return dbQuery {
            UserSocialsTable
                .selectAll().where { UserSocialsTable.userId eq userId }
                .map { row ->
                    Social(
                        socialName = row[UserSocialsTable.socialName],
                        socialLink = row[UserSocialsTable.socialLink]
                    )
                }
        }
    }

    // FOLLOW
    suspend fun followUser(userId: Int, followedUserId: Int) {
        dbQuery {
            UserFollowedProfilesTable.insertIgnore {
                it[UserFollowedProfilesTable.userId] = userId
                it[UserFollowedProfilesTable.followedUserId] = followedUserId
            }
        }
    }

    suspend fun unfollowUser(userId: Int, followedUserId: Int) {
        dbQuery {
            UserFollowedProfilesTable.deleteWhere {
                (UserFollowedProfilesTable.userId eq userId) and
                        (UserFollowedProfilesTable.followedUserId eq followedUserId)
            }
        }
    }

    suspend fun getFollowerList(userId: Int): List<UserInfo> {
        return dbQuery {
            val query = UserInfoTable.join(
                UserFollowedProfilesTable,
                JoinType.INNER,
                onColumn = UserInfoTable.userId,
                otherColumn = UserFollowedProfilesTable.followedUserId
            )

            query.select(UserInfoTable.columns)
                .where { UserFollowedProfilesTable.userId eq userId.toInt() }

                .map { row ->
                    UserInfo(
                        userId = row[UserInfoTable.userId],
                        firstName = row[UserInfoTable.firstName],
                        lastName = row[UserInfoTable.lastName],
                        age = row[UserInfoTable.age],
                        bio = row[UserInfoTable.bio],
                        country = row[UserInfoTable.country],
                        city = row[UserInfoTable.city],
                        occupation = row[UserInfoTable.occupation],
                        photo = row[UserInfoTable.photo],
                        interests = row[UserInfoTable.interests]

                    )
                }
        }
    }

    suspend fun getSupportedLanguagesList(): List<Language> {
        return dbQuery {
            LanguagesTable.selectAll().map { row ->
                Language(
                    id = row[LanguagesTable.id],
                    language = row[LanguagesTable.language],
                    nativeName = row[LanguagesTable.nativeName]
                )
            }.toList()
        }
    }

    suspend fun updateUserLanguages(userId: Int, nativeNames: List<String>) {
        dbQuery {
            val languageIds = LanguagesTable
                .select(LanguagesTable.id)
                .where { LanguagesTable.nativeName inList nativeNames }
                .map { it[LanguagesTable.id] }


            UserLanguagesTable.deleteWhere { UserLanguagesTable.userId eq userId }

            languageIds.forEach { languageId ->
                UserLanguagesTable.insert {
                    it[UserLanguagesTable.userId] = userId
                    it[UserLanguagesTable.languageId] = languageId
                }
            }
        }
    }

    suspend fun getUserLanguages(userId: Int): List<Language> {
        return dbQuery {
            val languages = (UserLanguagesTable innerJoin LanguagesTable)
                .select(LanguagesTable.id, LanguagesTable.language, LanguagesTable.nativeName)
                .where { UserLanguagesTable.userId eq userId }
                .map {
                    Language(
                        id = it[LanguagesTable.id],
                        language = it[LanguagesTable.language],
                        nativeName = it[LanguagesTable.nativeName]
                    )
                }
            languages
        }
    }
}

suspend fun fillInterestTags() {
    dbQuery {
        val count = InterestsTagsTable.selectAll().count()
        if (count == 0L) {
            val predefinedTags = listOf(
                "Видео", "Чтение", "Искусство", "Фотография", "Декоративно-прикладное искусство", "Ремесла", "Музыка",
                "Вокал", "Игра на музыкальных инструментах", "Диджеинг", "Композиция", "Командный спорт", "Индивидуальный спорт",
                "Экстремальный спорт", "Фитнес", "Единоборства", "Туризм", "Программирование", "Искусственный интеллект",
                "Робототехника", "Естественные науки", "Космос", "Электроника", "Кулинария", "Выпечка", "Национальные кухни",
                "Здоровое питание", "Напитки", "Садоводство", "Цветоводство", "Животные", "Экология", "Охота и рыбалка",
                "Литература", "Писательство", "Поэзия", "Блогинг", "Журналистика", "Кино", "Видеосъемка", "Анимация",
                "Подкасты", "Настольные игры", "Компьютерные игры", "Ролевые игры", "Интеллектуальные игры", "Путешествия",
                "Кемпинг", "Экотуризм", "Гастрономический туризм", "Мода", "Стиль", "Бьюти", "Татуировки и пирсинг",
                "Психология", "Саморазвитие", "Медитация", "Коучинг", "История", "Археология", "Философия", "Религия",
                "Архитектура", "Автомобили", "Мотоциклы", "Авиация", "Железная дорога", "Предпринимательство", "Инвестиции",
                "Маркетинг", "Фриланс", "Изучение языков", "Образование", "Наука", "Лингвистика", "Волонтерство", "Политика",
                "Социология", "Права человека", "Здоровый образ жизни", "Медицина", "Диетология", "Семья", "Воспитание детей",
                "Домоводство", "Дизайн интерьера", "Коллекционирование", "Косплей", "Головоломки", "Фокусы"
            )
            InterestsTagsTable.batchInsert(predefinedTags) { tag ->
                this[InterestsTagsTable.tag] = tag
            }
        }
    }
}
from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity


class NLPRecommender:
    def __init__(self):
        self.model = SentenceTransformer('paraphrase-MiniLM-L6-v2')

    def combine_user_data(self, user):
        return f"Bio: {user['bio']}. Interests: {user['interests']}. Occupation: {user['occupation']}."

    def recommend_users(self, user_id, users_data, top_k=20):
        current_user = next((user for user in users_data if user['id'] == user_id), None)
        if not current_user:
             return []

        user_text = self.combine_user_data(current_user)

        filtered_data = [u for u in users_data if u['id'] != user_id]
        if not filtered_data:
             return []

        other_user_texts = [self.combine_user_data(u) for u in filtered_data]
        embeddings = self.model.encode([user_text] + other_user_texts)

        if len(embeddings) < 2:
            return []

        sim_scores = cosine_similarity([embeddings[0]], embeddings[1:]).flatten()

        user_scores = [
            {"id": filtered_data[i]['id'], "score": float(sim_scores[i])}
            for i in range(len(filtered_data))
        ]

        top_users = sorted(user_scores, key=lambda x: x['score'], reverse=True)[:top_k]

        return top_users


app = Flask(__name__)

recommender = NLPRecommender()


@app.route('/recommendations_with_scores/', methods=['POST'])
def get_recommendations_with_scores():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid JSON"}), 400

    user_id = data.get('user_id')
    users_data = data.get('users_data')
    top_k = data.get('top_k', 20)

    if user_id is None or users_data is None:
         return jsonify({"error": "Missing user_id or users_data"}), 400

    try:
        recommendations_with_scores = recommender.recommend_users(user_id, users_data, top_k)
        return jsonify({"recommendations": recommendations_with_scores})
    except Exception as e:
        print(f"Error: {e}")
        return jsonify({"error": "Internal server error"}), 500

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8085)

from flask import Flask, jsonify
from flask_cors import CORS
import boto3
from botocore.exceptions import ClientError
import requests
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Initialize DynamoDB
dynamodb = boto3.resource(
    'dynamodb',
    aws_access_key_id='',
    aws_secret_access_key='',
    region_name='us-west-1'
)

news_table = dynamodb.Table('StockMarket1')

# News API Key (Replace with your NewsAPI key)
NEWS_API_KEY = ''

# OpenAI API Key (Replace with your OpenAI API Key)
OPENAI_API_KEY = ''


# Fetch stock news from News API based on the stock symbol
# Fetch stock news from News API based on the stock symbol
def fetch_stock_news(symbol):
    url = f'https://newsapi.org/v2/everything?q={symbol}&apiKey={NEWS_API_KEY}'

    # Make a request to the News API
    response = requests.get(url)

    if response.status_code != 200:
        print(f"Error fetching news for {symbol}: {response.status_code}")
        return []

    # Parse the JSON response from the News API
    news_data = response.json().get('articles', [])

    # Extract relevant fields (title, url, and published date)
    stock_news = []
    for article in news_data:
        stock_news.append({
            'title': article['title'],
            'url': article['url'],
            'scraped_at': str(datetime.now())
        })

    return stock_news[:20]  # Limit to the first 5 news articles


# Use OpenAI GPT-3/4 to analyze the news articles and generate a stock recommendation without mentioning the articles
def generate_stock_recommendation_openai(news_articles):
    # Concatenate the titles of the articles to provide context for the AI model without directly referencing them
    articles_summary = "\n".join([f"{article['title']}" for article in news_articles])

    # OpenAI prompt without explicitly mentioning the news articles in the output
    prompt = (
        f"Given the following recent developments about a stock:\n\n{articles_summary}\n\n"
        "Based on this, provide a recommendation for the stock (Buy, Sell, Hold) and a short explanation, "
        "but do not mention the articles directly,just prefix with \"based on current news\"."
    )

    # OpenAI API URL for completion
    url = "https://api.openai.com/v1/chat/completions"

    # Headers for the request
    headers = {
        'Authorization': f'Bearer {OPENAI_API_KEY}',  # Ensure the API key is properly included
        'Content-Type': 'application/json'
    }

    # Data to be sent to OpenAI API with updated model
    data = {
        "model": "gpt-3.5-turbo",  # Use "gpt-3.5-turbo" or "gpt-4" depending on your access
        "messages": [
            {"role": "system", "content": "You are a helpful financial assistant."},
            {"role": "user", "content": prompt}
        ],
        "max_tokens": 150,
        "temperature": 0.7
    }

    try:
        # Make a POST request to the OpenAI API
        response = requests.post(url, headers=headers, json=data)
        
        if response.status_code != 200:
            print(f"Error fetching recommendation from OpenAI API: {response.status_code}")
            return "Could not generate recommendation."

        # Extract the recommendation from OpenAI's response
        recommendation_data = response.json()
        recommendation = recommendation_data['choices'][0]['message']['content'].strip()
        return recommendation

    except Exception as e:
        print(f"Error generating recommendation: {e}")
        return "Could not generate recommendation."


# Store the news articles in DynamoDB
def store_news_in_dynamodb(symbol, news_articles):
    try:
        # Update the DynamoDB item for the given symbol
        news_table.update_item(
            Key={'Symbol': symbol.upper()},
            UpdateExpression="SET News = list_append(if_not_exists(News, :empty_list), :new_news)",
            ExpressionAttributeValues={
                ':new_news': [{'S': article['title']} for article in news_articles],
                ':empty_list': []
            }
        )
        print(f"Successfully updated news for {symbol}.")
    except ClientError as e:
        print(f"Could not update news: {e.response['Error']['Message']}")


# Fetch stock data and news when the frontend requests it
@app.route('/api/stock/<symbol>', methods=['GET'])
def get_stock(symbol):
    try:
        # Query DynamoDB for stock data
        response = news_table.get_item(Key={'Symbol': symbol.upper()})
        stock_data = response.get('Item')

        if not stock_data:
            return jsonify({'error': 'Stock not found'}), 404

        # Fetch fresh news for the symbol from NewsAPI
        fetched_news = fetch_stock_news(symbol)

        # Store fetched news in DynamoDB
        store_news_in_dynamodb(symbol, fetched_news)

        # Generate stock recommendation using OpenAI
        stock_recommendation = generate_stock_recommendation_openai(fetched_news)

        # Return only the recommendation
        return jsonify({
            'recommendation': stock_recommendation  # Only return the recommendation
        })

    except ClientError as e:
        return jsonify({'error': str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True)
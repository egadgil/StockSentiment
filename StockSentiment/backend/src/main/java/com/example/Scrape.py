import requests
from bs4 import BeautifulSoup

# Function to scrape Yahoo Finance news for a stock symbol
def scrape_and_store_yahoo_finance(symbol):
    url = f'https://finance.yahoo.com/quote/{symbol}/news?p={symbol}'
    response = requests.get(url)

    if response.status_code == 200:
        soup = BeautifulSoup(response.text, 'html.parser')
        articles = soup.find_all('h3')

        if not articles:
            print("No news articles found on the page.")
            return

        for article in articles:
            title = article.get_text()
            link_tag = article.find('a')
            if link_tag and 'href' in link_tag.attrs:
                link = "https://finance.yahoo.com" + link_tag['href']
                print(f"Title: {title}\nLink: {link}\n")

    else:
        print(f"Error fetching news: {response.status_code}")

# Example usage
symbol = "AAPL"
scrape_and_store_yahoo_finance(symbol)

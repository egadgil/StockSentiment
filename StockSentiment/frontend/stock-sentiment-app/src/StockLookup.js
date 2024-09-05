import React, { useState } from 'react';

const StockLookup = () => {
    const [symbol, setSymbol] = useState('');  // Holds the stock symbol input
    const [recommendation, setRecommendation] = useState('');  // Holds the OpenAI recommendation
    const [loading, setLoading] = useState(false);  // Shows loading state
    const [error, setError] = useState('');  // Holds any error messages

    // Fetch the stock recommendation for the entered symbol
    const fetchStockRecommendation = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await fetch(`http://127.0.0.1:5000/api/stock/${symbol}`);  // Point to Flask backend
            if (!response.ok) throw new Error('Failed to fetch recommendation');
            const data = await response.json();
            setRecommendation(data.recommendation);  // Set the OpenAI recommendation
        } catch (err) {
            console.error(err);  // Log error to console for debugging
            setError(err.message);  // Handle any errors
        } finally {
            setLoading(false);  // Stop loading
        }
    };

    return (
        <div>
            <h1>Stock Lookup</h1>
            <div>
                <input 
                    type="text" 
                    value={symbol} 
                    onChange={(e) => setSymbol(e.target.value)} 
                    placeholder="Enter stock symbol"
                    style={{ padding: '10px', fontSize: '16px' }}
                />
                <button onClick={fetchStockRecommendation} disabled={loading} style={{ marginLeft: '10px', padding: '10px', fontSize: '16px' }}>
                    {loading ? 'Loading...' : 'Look up Stock'}  {/* Show loading text */}
                </button>
            </div>

            {error && <p style={{ color: 'red' }}>{error}</p>}  {/* Show error messages */}

            {recommendation && (
                <div>
                    <h3>AI Stock Recommendation</h3>
                    <p>{recommendation}</p>  {/* Display the recommendation from OpenAI */}
                </div>
            )}
        </div>
    );
};

export default StockLookup;

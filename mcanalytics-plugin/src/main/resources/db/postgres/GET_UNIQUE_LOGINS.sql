SELECT date_trunc('day', instant) AS login_day, COUNT(DISTINCT id) AS login_count
FROM NewPlayerLogin
WHERE instant BETWEEN ? AND ?
GROUP BY login_day;
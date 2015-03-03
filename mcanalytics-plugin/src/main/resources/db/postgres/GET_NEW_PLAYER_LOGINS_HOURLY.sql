SELECT date_trunc('hour', date_joined) AS hour_joined, COUNT(id)
FROM NewPlayerLogin
WHERE date_joined BETWEEN ? AND ?
GROUP BY hour_joined;
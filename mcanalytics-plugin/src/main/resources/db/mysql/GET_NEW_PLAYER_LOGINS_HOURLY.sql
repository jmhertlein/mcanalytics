SELECT STR_TO_DATE(DATE_FORMAT(date_joined, '%Y-%m-%d-%H'), '%Y-%m-%d-%H') AS hour_joined, COUNT(id) AS login_count
FROM NewPlayerLogin
WHERE date_joined BETWEEN ? AND ?
GROUP BY hour_joined;
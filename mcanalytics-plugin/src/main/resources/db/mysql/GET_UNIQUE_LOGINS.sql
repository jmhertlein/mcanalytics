SELECT STR_TO_DATE(DATE_FORMAT(instant, '%Y-%m-%d'), '%Y-%m-%d') AS login_day, COUNT(DISTINCT id) AS login_count
FROM PlayerLogin
WHERE instant BETWEEN ? AND ?
GROUP BY login_day;
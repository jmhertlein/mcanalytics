UPDATE NewPlayerLogin
SET bounced=false
WHERE id=? AND bounced=true;
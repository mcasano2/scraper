# scraper
Java 8 program to scrape nba team stats 

Usage:
-------
command line args sets date range, accepts either 1 date or 2 dates where startDate <= endDate

    Examples
     $ java -jar scraper.jar 20160101
     $ java -jar scraper.jar 20151225 20160105
     $ java -jar scraper.jar 20160101 20160101

If no args are found, date range is read from scraper.properties

If scraper.properties not found, scraper.properties is created with a date range set to yesterday

CSV format
-----------
gameInfoCSV

    gameId,gameDate,numQuarters,awayName,homeName,awayTotal,homeTotal
    
awayCSV

    gameId,0,awayFgm,awayFga,away3pm,away3pa,awayFtm,awayFta,awayOreb,awayDreb,awayReb,awayAst,awayPf,awayStl,awayTeamTO,awayBlk
    
homeCSV

    gameId,1,homeFgm,homeFga,home3pm,home3pa,homeFtm,homeFta,homeOreb,homeDreb,homeReb,homeAst,homePf,homeStl,homeTeamTO,homeBlk

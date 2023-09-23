-- 1. Check how many unique actors are present in IMDB dataset.
SELECT COUNT(DISTINCT CONCAT(first_name,last_name,gender)) FROM actors;

-- 2. Check how many movies are released between the year 1990s till 2000.
SELECT COUNT(*) FROM movies
WHERE year BETWEEN 1990 AND 2000;

-- 3. Find the list of genres of movies directed by Christopher Nolan.
SELECT genre 
	FROM directors d 
    INNER JOIN directors_genres dg
    ON d.id=dg.director_id 
	WHERE d.first_name='Christopher' AND d.last_name='Nolan'; 

-- 4. Find the list of all directors, and the movie name which are ranked between 8 to 9 and have a genre of Sci-Fi and Action.
SELECT CONCAT(d.first_name,' ',d.last_name) 'Director Name', m.name 'Movie Name' from movies m
INNER JOIN movies_directors md ON m.id=md.movie_id 
INNER JOIN directors d ON d.id=md.director_id 
WHERE m.rank BETWEEN 8 AND 9 
	AND m.id IN (SELECT movie_id 
		FROM movies_genres mg 
        WHERE genre='Sci-Fi' OR genre='Action'
        GROUP BY movie_id 
        HAVING COUNT(*)=2);

-- 5. Find the name of the movie in which the actor’s role is any doctor, and the movie has the highest number of roles of doctor.
SELECT m.name FROM movies m 
INNER JOIN roles r ON m.id=r.movie_id 
INNER JOIN actors a ON a.id=r.actor_id 
WHERE r.role LIKE '%doctor%'
GROUP BY m.id 
ORDER BY COUNT(*) DESC LIMIT 1; 

-- 6. Find the list of the movies that start the letter ‘f’.
SELECT name FROM movies
WHERE name LIKE 'f%';
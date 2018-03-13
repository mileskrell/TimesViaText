# Instructions for creating an SQLite database from CTA GTFS data

The data can be found [here](http://www.transitchicago.com/downloads/sch_data/google_transit.zip).

This file is updated roughly once a month.

## 1. Database structure setup

### routes

Import the `routes` table.

Remove rows with no `route_short_name`:

	DELETE FROM routes WHERE route_short_name = ''

Create a column called `route_sequence`, so columns are as follows:

	route_sequence		INTEGER
	route_id			TEXT
	route_long_name		TEXT

Give each route a number and add it to that route's `route_sequence`.

This is so the routes can be sorted properly:

    49, 49B, X49...

### stop_times

Import the `stop_times` table.

Columns are as follows:

	trip_id	INTEGER
	stop_id	INTEGER

### stops

Import the `stops` table.

Remove rows with no `stop_code`:

	DELETE FROM stops WHERE stop_code = ''

Remove extra data from `stop_desc`:

	UPDATE stops SET stop_desc =
		REPLACE(
			stop_desc,
			stop_desc,
			SUBSTR(
				stop_desc,
				LENGTH(stop_name) + 3,
				INSTR(SUBSTR(stop_desc, LENGTH(stop_name) + 3), ',') - 1
			)
		)

Use sqlitebrowser to find and fix the stop names of stops 572, 606, and 2990 (as of January 31, 2018).

Use sqlitebrowser to remove stops 2248, 9333, 16123, 17031, 17353, 17508, 17509, 17538, 17673, and 18219 (as of January 31, 2018).

Rename `stop_desc` to `stop_dir`.

Columns are as follows:

	stop_id		INTEGER
	stop_name	TEXT
	stop_dir	TEXT
	stop_lat	REAL
	stop_lon	REAL

### trips

Import the `trips` table.

Columns are as follows:

	route_id	TEXT
	trip_id		INTEGER
	direction	TEXT

## 2. Index creation (to speed up the following queries)

	CREATE INDEX `stop_id_index` ON `stops` (`stop_id` )

	CREATE INDEX `stop_id_trip_id_index` ON `stop_times` (`trip_id` ,`stop_id` )

	CREATE INDEX `trip_id_route_id_index` ON `trips` (`route_id` ,`trip_id` )

## 3. Data migration (this is the long, tedious part)

	CREATE TABLE route_1_stops AS
		SELECT DISTINCT stop_id, direction
		FROM stops NATURAL JOIN stop_times NATURAL JOIN trips NATURAL JOIN routes
		WHERE route_id = '1'

Repeat for every route (probably either 126 or 128).

## 4.  Final steps

1. Remove all three indices.

2. Remove `trips` table and `stop_times` table.

3. Compact the database (it should be around 1 MB).

4. Move the database to `app/src/main/assets/databases/cta.db`.

5. Add the date of the data to `R.string.data_last_updated`.
web: java $JAVA_OPTS -Dserver.port=$PORT -jar target/transform-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=prod --db.user=$SPRING_DATASOURCE_USER --db.password=$SPRING_DATASOURCE_PASSWORD --db.r2dbc.url=r2dbc:${SPRING_DATASOURCE_URL} --db.jdbc.url=jdbc:${SPRING_DATASOURCE_URL}

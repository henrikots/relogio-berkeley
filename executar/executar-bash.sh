java -jar "../SincRelogio/dist/SincRelogio.jar" "-s" "localhost:3501" "12:10:00" "..\logs\relogio1_log.txt" &
java -jar "../SincRelogio/dist/SincRelogio.jar" "-s" "localhost:3502" "12:08:00" "..\logs\relogio2_log.txt" &
java -jar "../SincRelogio/dist/SincRelogio.jar" "-s" "localhost:3502" "12:06:00" "..\logs\relogio3_log.txt" &
java -jar "../SincRelogio/dist/SincRelogio.jar" "-s" "localhost:3502" "12:10:00" "..\logs\relogio4_log.txt" &
java -jar "../SincRelogio/dist/SincRelogio.jar" "-m" "localhost:3500" "12:05:00" "00:10:00" "..\SincRelogio\slaves.txt" "..\logs\master_log.txt"

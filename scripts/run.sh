./gradlew shadowJar -q && java -jar build/libs/winzig-compiler-1.0-SNAPSHOT-all.jar "$1" > test.abs
python ./machine/winzig-machine.py test.abs "$2"

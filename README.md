## Holt-Winters prognoses
Simple java with jafafx GUI realization of the Holt-Winters series with prognoses.

### Compilation
1) Make dir `bin` at root of project 
2) Compile sources with jdk 1.8 (jfx included)
```sh
javac -encoding utf8 -sourcepath ./src -d ./bin ./src/*.java
javac -encoding utf8 -sourcepath ./src -d ./bin ./src/services/*.java
javac -encoding utf8 -sourcepath ./src -d ./bin ./src/controllers/*.java
```
3) Copy files from `resources` dir into `/bin` (TODO)

### Execution
Run compiled project
```sh
java -Dfile.encoding=UTF-8 -classpath ./bin Main
```

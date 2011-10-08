CC = gcc
SOURCE = giyusit_sqlite_stub.c
TARGET = giyusit_sqlite_stub.so

all : ${TARGET}

${TARGET} : ${SOURCE}
	${CC} -shared -fPIC -o ${TARGET} ${SOURCE} 

clean:
	rm -f ${TARGET}
	

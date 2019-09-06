DIR_INC := ./include
DIR_SRC := ./src
DIR_OBJ := ./obj
DIR_BIN := ./bin

SRC := $(wildcard ${DIR_SRC}/*.c)
OBJ := $(patsubst %.c,${DIR_OBJ}/%.o,$(notdir ${SRC}))

CC := arm-linux-gnueabihf-gcc
INCLUDE := -I$(DIR_INC) -I/usr/local/mqtt_c_arm/include/
LDFLAGS := -L/usr/local/mqtt_c_arm/lib -l paho-mqtt3as -L/usr/local/openssl/lib -lcrypto -lssl -lpthread

$(DIR_BIN)/main:$(OBJ)
	$(CC) $(OBJ) -o $@ $(LDFLAGS)

$(DIR_OBJ)/%.o:$(DIR_SRC)/%.c
	$(CC) $< $(INCLUDE) -c -o $@

install:
	cp $(DIR_BIN)/main /home/hyr/arm_mount/

clean:
	rm $(DIR_OBJ)/*.o $(DIR_BIN)/main -f

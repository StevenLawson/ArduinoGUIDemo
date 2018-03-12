#define SERIAL_INPUT_BUFFER_LEN 150
#define CMD_START_CHAR '*'
#define CMD_TOKEN_DELIMITER ","

void setup()
{
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  Serial.begin(9600);

  Serial.println("@STARTED");
}

void loop()
{
  loopSerialIO();
}

void loopSerialIO(void)
{
  static char serialBuffer[SERIAL_INPUT_BUFFER_LEN];
  static int serialBufferOffset = 0;

  while (Serial.available())
  {
    serialBuffer[serialBufferOffset] = Serial.read();
    if (serialBuffer[serialBufferOffset] == '\n')
    {
      serialBuffer[serialBufferOffset] = '\0';
      serialBufferOffset = 0;
      handleCommand(serialBuffer);
    }
    else
    {
      if (serialBufferOffset < (SERIAL_INPUT_BUFFER_LEN - 1))
      {
        serialBufferOffset++;
      }
    }
  }
}

void handleCommand(char *buffer)
{
  if (buffer[0] != CMD_START_CHAR)
  {
    Serial.print("@ERROR,INVALID_COMMAND,");
    Serial.println(buffer);
    return;
  }

  char *command = strtok(buffer, CMD_TOKEN_DELIMITER);

  if (strcmp(command, "*PING") == 0)
  {
    Serial.println("@PONG");
  }
  else if (strcmp(command, "*BLINK_LED") == 0)
  {
    char *valueStr = strtok(NULL, CMD_TOKEN_DELIMITER);
    if (valueStr != NULL)
    {
      int numBlinks = atoi(valueStr);
      Serial.println("@BLINK_LED,START");
      blinkLED(numBlinks);
      Serial.println("@BLINK_LED,FINISH");
    }
    else
    {
      Serial.println("@BLINK_LED,ERROR,INVALID_NUM_BLINKS");
    }
  }
  else
  {
    Serial.print("@ERROR,INVALID_COMMAND,");
    Serial.println(command);
  }
}

void blinkLED(int numBlinks)
{
  for (int i = 0; i < numBlinks; i++)
  {
    digitalWrite(LED_BUILTIN, HIGH);
    delay(250);
    digitalWrite(LED_BUILTIN, LOW);
    if (i < numBlinks - 1)
    {
      delay(250);
    }
  }
}

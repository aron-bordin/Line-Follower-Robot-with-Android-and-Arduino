#include <SoftwareSerial.h>
#include "Bluetooth.h"
#include "Motor.h"
#include "Ultrasonic.h"

Ultrasonic *ult = new Ultrasonic(A5, A4); //TRIGGER_PIN, ECHO_PIN
Motor *motor = new Motor(6, 7, 5, 8, 9, 11); //IN1, IN2, ENA, IN3, IN4, ENB
Bluetooth *blue = new Bluetooth(2, 3); //RX PIN, TX PIN

void setup(){
	Serial.begin(9600);

	blue->setName("Timotheo"); //Set the bluetooth name
	blue->setPIN(6666); //set the PIN NUMBER
	blue->setMessageEnd('#'); //use the # as the end of the message
	blue->setupBluetooth();  //applay the changes to the bluetooth module

	motor->setSpeed(180); //set the motor speed, value from 0 to 255
}

void loop(){
	String msg = blue->read();
	if(msg.length() > 1) //check the length of the message
		Serial.println(msg); //shows it on serial
		readMsg(msg); //analyze the message
	delay(20); //wait 20 ms
}

void callMethod(int cod, int arg1){
	switch (cod) {
		case 1: motor->goFront(); break;
		case 2: motor->goRight(); break;
		case 3: motor->goRightStrong(); break;
		case 4: motor->goLeft(); break;
		case 5: motor->goLeftStrong(); break;
		case 6: motor->goBack(); break;
		case 7: motor->goStop(); break;
		case 8: returnValue(arg1, (int)ult->readDistanceCM()); break;
		case 9: delay(arg1); break;
		case 99: returnValue(1, 99); break;
	}
}

void returnValue(int id, int value){
	char Id[5], Val[5], msg[1024];
	sprintf(Id, "%d", id); 
	sprintf(Val, "%d", value);
	strcpy(msg, Id);
	strcat(msg, "@");
	strcat(msg, Val);
	strcat(msg, "#");

	blue->send(msg); //send the message in the format "ID@VALUE#"
}

void readMsg(String msg){
	String SB, TB; //second byte and third byte, the parameters in the message
	int iSB, iTB;
	int i;
	switch (msg.charAt(0)) { //we read the first number to check the action
	    case '1'://not used in this tutorial. Set an Android PIN value
	      break;
	    case '2'://not used in this tutorial. Get an Android PIN value
	      break;
	    
	    case '3': //call a method
	    	for(i = 2; i < msg.length(); i++){//we start at pos 2, because the msg[0] is 3 and msg[1] is '@'
	    		if(msg.charAt(i) == '#') //end of the message
	    			break;
	    		if(msg.charAt(i) == '@') //read the next parameter
	    			for(int k = i+1; k < msg.length(); k++){
	    				if(msg.charAt(k) == '#')
	    					break;
	    				TB += msg.charAt(k);
	    			}
	    		SB += msg.charAt(i);
	    	}
	    	iSB = SB.toInt();
	    	iTB = TB.toInt();
	    	callMethod(iSB, iTB); //we call the method sending the second and third value received
	    break;
	}
}
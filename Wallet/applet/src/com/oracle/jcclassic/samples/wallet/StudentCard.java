/** 
 * Copyright (c) 1998, 2021, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

/*
 * @(#)Wallet.java	1.11 06/01/03
 */

package com.oracle.jcclassic.samples.wallet;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;

public class StudentCard extends Applet {

	/* constants declaration */

    // code of CLA byte in the command APDU header
    final static byte Student_CLA = (byte) 0x80;

    // codes of INS byte in the command APDU header
    final static byte VERIFY = (byte) 0x20;
    final static byte INSERT_GRADE = (byte) 0x70;
    final static byte SEND_GRADES = (byte) 0x80;

    // maximum balance
    final static short MAX_BALANCE = 0x7FFF;
    // maximum transaction amount
    final static byte MAX_TRANSACTION_AMOUNT = 127;

    // maximum number of incorrect tries before the
    // PIN is blocked
    final static byte PIN_TRY_LIMIT = (byte) 0x03;
    // maximum size PIN
    final static byte MAX_PIN_SIZE = (byte) 0x08;

    // signal that the PIN verification failed
    final static short SW_VERIFICATION_FAILED = 0x6300;
    // signal the the PIN validation is required
    // for a credit or a debit transaction
    final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;
    // signal invalid transaction amount
    // amount > MAX_TRANSACTION_AMOUNT or amount < 0
    final static short SW_INVALID_TRANSACTION_AMOUNT = 0x6A83;

    // signal that the balance exceed the maximum
    final static short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;
    // signal the the balance becomes negative
    final static short SW_NEGATIVE_BALANCE = 0x6A85;

    /* instance variables declaration */
    OwnerPIN pin;
    short studentId = 1;
    short i = 0;
    Courses[] courses = new Courses[100];

    private StudentCard(byte[] bArray, short bOffset, byte bLength) {

        // It is good programming practice to allocate
        // all the memory that an applet needs during
        // its lifetime inside the constructor
        pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);

        byte iLen = bArray[bOffset]; // aid length
        bOffset = (short) (bOffset + iLen + 1);
        byte cLen = bArray[bOffset]; // info length
        bOffset = (short) (bOffset + cLen + 1);
        byte aLen = bArray[bOffset]; // applet data length

        // The installation parameters contain the PIN
        // initialization value
        pin.update(bArray, (short) (bOffset + 1), aLen);
        register();
        

    } // end of the constructor

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        // create a Wallet applet instance
        new StudentCard(bArray, bOffset, bLength);
    } // end of install method

    @Override
    public boolean select() {

        // The applet declines to be selected
        // if the pin is blocked.
        if (pin.getTriesRemaining() == 0) {
            return false;
        }

        return true;

    }// end of select method

    @Override
    public void deselect() {

        // reset the pin value
        pin.reset();

    }

    @Override
    public void process(APDU apdu) {

        // APDU object carries a byte array (buffer) to
        // transfer incoming and outgoing APDU header
        // and data bytes between card and CAD

        // At this point, only the first header bytes
        // [CLA, INS, P1, P2, P3] are available in
        // the APDU buffer.
        // The interface javacard.framework.ISO7816
        // declares constants to denote the offset of
        // these bytes in the APDU buffer

        byte[] buffer = apdu.getBuffer();
        // check SELECT APDU command

        if (apdu.isISOInterindustryCLA()) {
            if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
                return;
            }else {
            	ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            }
            
        }

        // verify the reset of commands have the
        // correct CLA byte, which specifies the
        // command structure
        if (buffer[ISO7816.OFFSET_CLA] != Student_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
        switch (buffer[ISO7816.OFFSET_INS]) {
        	case INSERT_GRADE:
        		insertGrade(apdu);
        		return;
        	case SEND_GRADES:
        		sendGrades(apdu);
        		return;
            case VERIFY:
                verify(apdu);
                return;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }

    } // end of process method
    
    private void insertGrade(APDU apdu) {
    	//p1 - course id
    	//dataIn - grade
    	//respond with studentID
    	
    	byte[] buffer = apdu.getBuffer();
    	
    	short le = apdu.setOutgoing();
    	if (le < 1) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
    	
    	short course_id = buffer[ISO7816.OFFSET_P1];
    	short grade = buffer[ISO7816.OFFSET_CDATA];
    	courses[i] = new Courses(course_id, grade);
    	i++;
    	
    	apdu.setOutgoingLength((byte) 2);
    	buffer[0] = (byte) (studentId >> 8);
        buffer[1] = (byte) (studentId & 0xFF);
    	
    	apdu.sendBytes((short) 0, (short) 2);	
    }
    
    
    private void sendGrades(APDU apdu) {
    	
    	byte[] buffer = apdu.getBuffer();
    	
    	short le = apdu.setOutgoing();
    	if (le < 1) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
    	
    	
    	short length = (short) (i * 4);
    	apdu.setOutgoingLength((byte) length);
    	
    	short k = 0;
    	for(short j = 0; j < i; j++) {
    		buffer[k] = (byte) (courses[j].id >> 8);
    		buffer[(short) (k+1)] = (byte) (courses[j].id & 0xFF);
    		buffer[(short) (k+2)] = (byte) (courses[j].grade >> 8);
    		buffer[(short) (k+3)] = (byte) (courses[j].grade & 0xFF);
    		k += 4;
    	}
    	
    	apdu.sendBytes((short) 0, (short) length);	
    }

    private void verify(APDU apdu) {

        byte[] buffer = apdu.getBuffer();
        // retrieve the PIN data for validation.
        byte byteRead = (byte) (apdu.setIncomingAndReceive());

        // check pin
        // the PIN data is read into the APDU buffer
        // at the offset ISO7816.OFFSET_CDATA
        // the PIN data length = byteRead
        if (pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
            ISOException.throwIt(SW_VERIFICATION_FAILED);
        }

    } // end of validate method
} // end of class Wallet
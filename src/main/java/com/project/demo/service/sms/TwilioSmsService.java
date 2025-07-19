package com.project.demo.service.sms;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioSmsService {
    public static final String ACCOUNT_SID = "your_account_sid";
    public static final String AUTH_TOKEN = "your_auth_token";

    public TwilioSmsService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public String sendSms(String to, String messageBody, String from) {
        Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(from),
                messageBody
        ).create();
        return message.getSid();
    }
}
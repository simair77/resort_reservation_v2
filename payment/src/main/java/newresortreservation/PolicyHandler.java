package newresortreservation;

import newresortreservation.config.kafka.KafkaProcessor;

import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationRegistered_PaymentRequestPolicy(@Payload ReservationRegistered reservationRegistered){

        if(!reservationRegistered.validate()) return;

        System.out.println("\n\n##### listener PaymentRequestPolicy : " + reservationRegistered.toJson() + "\n\n");

        Payment payment = new Payment();
        payment.setReservId(reservationRegistered.getId());
        payment.setResortPrice(reservationRegistered.getResortPrice());
        payment.setReservStatus(reservationRegistered.getResortStatus());
        paymentRepository.save(payment);
        
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_PaymentCancelPolicy(@Payload ReservationCanceled reservationCanceled){

        if(!reservationCanceled.validate()) return;

        System.out.println("\n\n##### listener PaymentCancelPolicy : " + reservationCanceled.toJson() + "\n\n");

        // 리조트 상태를 예약 가능 상태로 변경
        
        Optional<Payment> myPageOptional = paymentRepository.findByReservId(reservationCanceled.getId());

        if( myPageOptional.isPresent()) {
            Payment payment = myPageOptional.get();
            payment.setReservStatus("Cancelled");
            paymentRepository.save(payment);
        }

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}

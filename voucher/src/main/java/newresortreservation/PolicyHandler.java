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
    @Autowired VoucherRepository voucherRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_VoucherRequestPolicy(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        System.out.println("\n\n##### listener VoucherRequestPolicy : " + paymentApproved.toJson() + "\n\n");

        Voucher voucher = new Voucher();
        voucher.setReservId(paymentApproved.getReservId());
        voucher.setVoucherStatus(paymentApproved.getReservStatus());
        voucherRepository.save(voucher);

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCancelled_VoucherCancelPolicy(@Payload PaymentCancelled paymentCancelled){

        if(!paymentCancelled.validate()) return;

        System.out.println("\n\n##### listener VoucherCancelPolicy : " + paymentCancelled.toJson() + "\n\n");

        Optional<Voucher> myPageOptional = voucherRepository.findByReservId(paymentCancelled.getReservId());

        if( myPageOptional.isPresent()) {
            Voucher voucher = myPageOptional.get();
            voucher.setVoucherStatus("Cancelled");
            voucherRepository.save(voucher);
        }

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}

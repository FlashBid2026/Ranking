package FlashBid_Ranking.Ranking.Bid.consumer;

import FlashBid_Ranking.Ranking.Bid.dto.BidEventDto;
import FlashBid_Ranking.Ranking.Bid.service.BidService;
import FlashBid_Ranking.Ranking.Config.RabbitConsumerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BidMessageConsumer {

  private final BidService rankingService;
  private final ObjectMapper objectMapper;

  @RabbitListener(queues = RabbitConsumerConfig.QUEUE_NAME)
  public void consumeBidMessage(String messagePayload) {
    try {
      log.info("RabbitMQ 메시지 수신: {}", messagePayload);

      BidEventDto event = objectMapper.readValue(messagePayload, BidEventDto.class);

      rankingService.updateRanking(event);

      //  SSE를 통해 클라이언트에 실시간 알림 전송 로직 추가

    } catch (Exception e) {
      log.error("메시지 처리 중 오류 발생: {}", e.getMessage());
    }
  }
}
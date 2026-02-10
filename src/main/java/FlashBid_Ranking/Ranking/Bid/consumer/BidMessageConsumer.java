package FlashBid_Ranking.Ranking.Bid.consumer;

import FlashBid_Ranking.Ranking.Bid.dto.BidEventDto;
import FlashBid_Ranking.Ranking.Bid.service.BidService;
import FlashBid_Ranking.Ranking.Config.RabbitConsumerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BidMessageConsumer {

  private final BidService rankingService;


  @RabbitListener(queues = RabbitConsumerConfig.QUEUE_NAME)
  public void consumeBidMessage(BidEventDto event) {
    try {
      log.info("RabbitMQ 메시지 수신: {}", event);
      rankingService.updateRanking(event);
    } catch (Exception e) {
      log.error("메시지 처리 중 오류 발생: {}", e.getMessage());
    }
  }
}
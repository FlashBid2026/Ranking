package FlashBid_Ranking.Ranking.Bid.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import FlashBid_Ranking.Ranking.Bid.dto.BidEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private ZSetOperations<String, Object> zSetOperations;

  @InjectMocks
  private BidService bidService;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
  }

  @Test
  @DisplayName("입찰 이벤트를 받으면 Redis ZSet에 랭킹을 추가한다")
  void updateRanking_addsToZSet() {
    BidEventDto event = new BidEventDto(1L, 100L, 50000L, "user1");

    bidService.updateRanking(event);

    verify(zSetOperations).add("auction:ranking:1", "user1", 50000.0);
  }

  @Test
  @DisplayName("랭킹 업데이트 후 상위 100개만 유지하도록 나머지를 제거한다")
  void updateRanking_removesExcessEntries() {
    BidEventDto event = new BidEventDto(1L, 100L, 50000L, "user1");

    bidService.updateRanking(event);

    verify(zSetOperations).removeRange("auction:ranking:1", 0, -101);
  }

  @Test
  @DisplayName("경매 아이템 ID에 따라 올바른 Redis 키가 생성된다")
  void updateRanking_usesCorrectKeyForDifferentItems() {
    BidEventDto event = new BidEventDto(999L, 200L, 75000L, "bidder99");

    bidService.updateRanking(event);

    verify(zSetOperations).add("auction:ranking:999", "bidder99", 75000.0);
    verify(zSetOperations).removeRange("auction:ranking:999", 0, -101);
  }

  @Test
  @DisplayName("입찰가가 ZSet의 score로 정확히 변환된다")
  void updateRanking_convertsLongPriceToDoubleScore() {
    BidEventDto event = new BidEventDto(5L, 300L, 123456789L, "highBidder");

    bidService.updateRanking(event);

    verify(zSetOperations).add("auction:ranking:5", "highBidder", 123456789.0);
  }

  @Test
  @DisplayName("동일 경매방에 여러 입찰이 들어오면 각각 ZSet에 추가된다")
  void updateRanking_handlesMultipleBidsForSameItem() {
    BidEventDto event1 = new BidEventDto(1L, 100L, 10000L, "userA");
    BidEventDto event2 = new BidEventDto(1L, 200L, 20000L, "userB");

    bidService.updateRanking(event1);
    bidService.updateRanking(event2);

    verify(zSetOperations).add("auction:ranking:1", "userA", 10000.0);
    verify(zSetOperations).add("auction:ranking:1", "userB", 20000.0);
  }
}

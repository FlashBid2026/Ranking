package FlashBid_Ranking.Ranking.Bid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BidEventDto {
  private Long itemId;
  private Long winnerId;
  private Long bidPrice;
  private String winnerNickname;
}
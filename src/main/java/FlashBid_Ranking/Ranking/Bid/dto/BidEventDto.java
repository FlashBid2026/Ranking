package FlashBid_Ranking.Ranking.Bid.dto;

public record BidEventDto(
    Long itemId,
    Long winnerId,
    Long bidPrice,
    String winnerNickname
) {}
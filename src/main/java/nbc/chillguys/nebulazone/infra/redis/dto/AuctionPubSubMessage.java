package nbc.chillguys.nebulazone.infra.redis.dto;

public record AuctionPubSubMessage(
	Long auctionId,
	String updateType,
	Object data
) {

	public static AuctionPubSubMessage of(Long auctionId, String updateType, Object data) {
		return new AuctionPubSubMessage(
			auctionId,
			updateType,
			data
		);
	}

	public static String getChannelName(Long auctionId, String updateType) {
		return "auction:" + updateType + ":" + auctionId;
	}
}

package nbc.chillguys.nebulazone.application.auction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.auction.service.AutoAuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
public class AutoAuctionService {

	private final AuctionDomainService auctionDomainService;
	private final UserDomainService userDomainService;
	private final TransactionDomainService txDomainService;
	private final ProductDomainService productDomainService;
	private final AutoAuctionDomainService autoAuctionDomainService;
	private final BidDomainService bidDomainService;

	@Transactional
	public void autoEndAuctionAndCreateTransaction(Long auctionId, Long productId) {

		Auction auction = auctionDomainService.findActiveAuctionById(auctionId);
		Product product = productDomainService.findActiveProductById(productId);
		Bid wonBid = bidDomainService.findBid(auction.getId());

		autoAuctionDomainService.endAuction(auctionId, wonBid);

		TransactionCreateCommand txCreateCommand =
			TransactionCreateCommand.of(wonBid.getUser(), product, product.getTxMethod().name(), wonBid.getPrice());

		txDomainService.createTransaction(txCreateCommand);
	}
}

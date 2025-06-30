package nbc.chillguys.nebulazone.application.auction.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.request.AuctionAdminSearchRequest;
import nbc.chillguys.nebulazone.application.auction.dto.request.AuctionAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.AuctionAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionAdminDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.product.entity.ProductImage;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;

@Service
@RequiredArgsConstructor
public class AuctionAdminService {
	private final AuctionAdminDomainService auctionAdminDomainService;
	private final AuctionRedisService auctionRedisService;

	public CommonPageResponse<AuctionAdminResponse> findAuctions(AuctionAdminSearchRequest request, Pageable pageable) {
		AuctionAdminSearchQueryCommand command = new AuctionAdminSearchQueryCommand(
			request.keyword(),
			request.deleted(),
			request.isWon()
		);
		Page<AuctionAdminInfo> infoPage = auctionAdminDomainService.findAuctions(command, pageable);
		return CommonPageResponse.from(infoPage.map(AuctionAdminResponse::from));
	}

	@Transactional
	public void updateAuction(Long auctionId, AuctionAdminUpdateRequest request) {
		AuctionAdminUpdateCommand command = AuctionAdminUpdateCommand.from(request);
		auctionRedisService.updateAdminAuction(auctionId, command);
		auctionAdminDomainService.updateAuction(auctionId, command);
	}

	@Transactional
	public void deleteAuction(Long auctionId) {
		auctionRedisService.deleteAdminAuction(auctionId);
		auctionAdminDomainService.deleteAuction(auctionId);
	}

	@Transactional
	public void restoreAuction(Long auctionId) {
		Auction restoredAuction = auctionAdminDomainService.restoreAuction(auctionId);

		Product product = restoredAuction.getProduct();
		User user = product.getSeller();
		ProductEndTime productEndTime = ProductEndTime.from(restoredAuction.getEndTime());

		List<String> imageUrls = product.getProductImages()
			.stream()
			.map(ProductImage::getUrl)
			.toList();

		CreateRedisAuctionDto redisDto =
			new CreateRedisAuctionDto(product, restoredAuction, user, productEndTime, imageUrls);

		auctionRedisService.createAuction(redisDto);
	}

}

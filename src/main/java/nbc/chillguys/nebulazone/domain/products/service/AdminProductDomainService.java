package nbc.chillguys.nebulazone.domain.products.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.products.dto.AdminProductInfo;
import nbc.chillguys.nebulazone.domain.products.dto.AdminProductSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class AdminProductDomainService {

	private final ProductRepository productRepository;

	public Page<AdminProductInfo> findProducts(AdminProductSearchQueryCommand command, Pageable pageable) {
		Page<Product> page = productRepository.searchProducts(command, pageable);
		return page.map(AdminProductInfo::from);
	}
}

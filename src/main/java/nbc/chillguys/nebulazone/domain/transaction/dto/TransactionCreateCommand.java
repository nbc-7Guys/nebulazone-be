package nbc.chillguys.nebulazone.domain.transaction.dto;

import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record TransactionCreateCommand(
	User user,
	Product product,
	String txMethod,
	Long price
) {

	public static TransactionCreateCommand of(User user, Product product, String name, Long price) {
		return new TransactionCreateCommand(user, product, name, price);
	}
}

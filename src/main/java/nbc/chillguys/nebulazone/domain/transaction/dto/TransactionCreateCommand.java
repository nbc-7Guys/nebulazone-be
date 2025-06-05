package nbc.chillguys.nebulazone.domain.transaction.dto;

import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record TransactionCreateCommand(
	User user,
	Product product,
	String txMethod
) {

	public static TransactionCreateCommand of(User user, Product product, String name) {
		return new TransactionCreateCommand(user, product, name);
	}
}

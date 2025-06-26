package nbc.chillguys.nebulazone.domain.transaction.dto;

import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record TransactionCreateCommand(
	User user,
	UserType userType,
	Product product,
	String txMethod,
	Long price
) {

	public static TransactionCreateCommand of(User user, UserType userType, Product product, String txMethod,
		Long price) {
		return new TransactionCreateCommand(user, userType, product, txMethod, price);
	}
}

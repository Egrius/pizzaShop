package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.cart.CartReadDto;
import by.egrius.pizzaShop.dto.cart_item.CartItemCreateDto;
import by.egrius.pizzaShop.dto.cart_item.CartItemReadDto;
import by.egrius.pizzaShop.dto.order.OrderCreateDto;
import by.egrius.pizzaShop.entity.CartItem;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.entity.PizzaSize;
import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.exception.CartItemNotFoundException;
import by.egrius.pizzaShop.exception.PizzaNotFoundException;
import by.egrius.pizzaShop.exception.PizzaSizeNotFoundException;
import by.egrius.pizzaShop.mapper.cart_item.CartItemReadMapper;
import by.egrius.pizzaShop.repository.CartItemRepository;
import by.egrius.pizzaShop.repository.PizzaRepository;
import by.egrius.pizzaShop.repository.PizzaSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final PizzaRepository pizzaRepository;
    private final PizzaSizeRepository pizzaSizeRepository;

    private final CartItemReadMapper cartItemReadMapper;

    public CartReadDto getCart(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);

        List<CartItemReadDto> itemDtos = items.stream()
                .map(cartItemReadMapper::map)
                .toList();

        BigDecimal totalMoney = calculateTotal(items);

        return new CartReadDto(itemDtos, totalMoney);
    }

    @Transactional
    public void addToCart(User user, CartItemCreateDto dto) {
        Pizza pizza = pizzaRepository.findById(dto.pizzaId())
                .orElseThrow(() -> new PizzaNotFoundException("Не найдена пицца по id: " + dto.pizzaId()));

        PizzaSize size = pizzaSizeRepository.findById(dto.pizzaSizeId())
                .orElseThrow(() -> new PizzaSizeNotFoundException("Не найден размер для указанной пиццы, id: " + dto.pizzaSizeId()));

        cartItemRepository.findByUserAndPizzaAndPizzaSize(user, pizza, size)
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + dto.quantity()),
                        () -> createNewCartItem(user, pizza, size, dto.quantity())
                );
    }

    @Transactional
    private void createNewCartItem(User user, Pizza pizza, PizzaSize size, Integer quantity) {
        CartItem item = new CartItem();
        item.setUser(user);
        item.setPizza(pizza);
        item.setPizzaSize(size);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateQuantity(User user, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .filter(ci -> ci.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new CartItemNotFoundException("Не найдена запись товара в корзине с id: " + itemId));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
        }
    }

    @Transactional
    public void removeItem(User user, Long itemId) {
        int deleted = cartItemRepository.deleteByUserAndId(user, itemId);
        if (deleted == 0) {
            throw new CartItemNotFoundException("Не найдена запись товара в корзине с id: " + itemId);
        }
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    private BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getPizzaSize().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
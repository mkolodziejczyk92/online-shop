package pl.sda.pol122.auctionservice.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.sda.pol122.auctionservice.model.CartItem;
import pl.sda.pol122.auctionservice.model.Product;
import pl.sda.pol122.auctionservice.model.SubmitPayment;
import pl.sda.pol122.auctionservice.services.CartService;
import pl.sda.pol122.auctionservice.services.ProductService;
import pl.sda.pol122.auctionservice.utils.PopUpMessage;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping()
public class CartController {

    private final CartService cartService;

    private final ProductService productService;

    @GetMapping("/addToCart/{productId}")
    public String addToCart(Model model, @PathVariable String productId) {
        Product productById = productService.getProductById(Integer.valueOf(productId));
        if (productById.getAvailableAmount() > 0) {
            cartService.addProductToCart(productById);
            model.addAttribute("product", productService.getProductById(Integer.valueOf(productId)));
            return "redirect:/product/details/{productId}";
        }
        return "redirect:/index";
    }

    @GetMapping("/buyNow/{productId}")
    public String buyNow(Model model, @PathVariable String productId) {
        Product productById = productService.getProductById(Integer.valueOf(productId));
        if (productById.getAvailableAmount() > 0) {
            cartService.addProductToCart(productById);
            model.addAttribute("product", productById);
            return "redirect:/cart";
        }
        return "redirect:/index";
    }

    @GetMapping("/cart/increase/{productId}")
    public String increaseQuantityOfProductsInCart(Model model, @PathVariable String productId) {
        Product productById = productService.getProductById(Integer.valueOf(productId));
        cartService.increaseProductQuantityInCart(productById);
        model.addAttribute("product", productById);
        return "redirect:/cart";
    }

    @GetMapping("/cart/{productId}")
    public String decreaseQuantityOfProductsInCart(Model model, @PathVariable String productId) {
        cartService.decreaseProductQuantityInCart(productService.getProductById(Integer.valueOf(productId)));
        model.addAttribute("product", productService.getProductById(Integer.valueOf(productId)));
        return "redirect:/cart";
    }

    @GetMapping("/deleteFromCart/{productId}")
    public String deleteFromCart(@PathVariable String productId) {
        Product productById = productService.getProductById(Integer.valueOf(productId));
        CartItem cartItem = new CartItem(productById, 0);
        cartService.deleteProductFromCart(cartItem);
        return "redirect:/cart";
    }


    @GetMapping(path = "/cart/checkout")
    public String loadCartCheckout(Model model) {
        if (cartService.getAllProducts().isEmpty() && cartService.checkIfStockIsAvailable()) {
            PopUpMessage.createPopUpMessage("You have empty cart or product is out of stock.");
            return "redirect:/cart";
        } else {
            model.addAttribute("submitPayment", new SubmitPayment());
            return "checkout";
        }
    }


    @PostMapping("/cart/checkout")
    public String submitUserPayment(@Valid @ModelAttribute SubmitPayment submitPayment,
                                    BindingResult result) {
        if(result.hasErrors()){
            return "checkout";
        }
        if (cartService.checkIfStockIsAvailable()) {
            List<CartItem> orderedProducts = cartService.getAllProducts();
            cartService.submitPayment(orderedProducts);
            cartService.clearCart();
        }else {
            PopUpMessage
                    .createPopUpMessage("Sorry! We don't have the quantity you want in one or more products.");
        }
        return "redirect:/cart";
    }

    @GetMapping(path = "/cart")
    public String loadUserCart(Model model) {
        List<CartItem> cartItems = cartService.getAllProducts();
        BigDecimal totalPriceOfCart = cartService.getTotalPriceOfCart();
        model.addAttribute("price", totalPriceOfCart);
        model.addAttribute("cartItems", cartItems);
        return "cart";
    }
}


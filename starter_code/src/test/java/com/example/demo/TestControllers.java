package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.hibernate.criterion.Order;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestControllers {
    private UserController userController;
    private final UserRepository userRepository = mock(UserRepository.class) ;
    private final CartRepository cartRepository = mock(CartRepository.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private ItemController itemController;
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private CartController cartController;
    private OrderController orderController;
    private final OrderRepository orderRepository = mock(OrderRepository.class);

    @Before
    public void setup(){
        userController = new UserController();
        itemController = new ItemController();
        cartController = new CartController();
        orderController = new OrderController();
        TestUtils.injectObjects(userController,"userRepository", userRepository);
        TestUtils.injectObjects(userController,"cartRepository", cartRepository);
        TestUtils.injectObjects(userController,"bCryptPasswordEncoder", encoder);
        TestUtils.injectObjects(itemController,"itemRepository",itemRepository);
        TestUtils.injectObjects(cartController,"itemRepository",itemRepository);
        TestUtils.injectObjects(cartController,"userRepository",userRepository);
        TestUtils.injectObjects(cartController,"cartRepository",cartRepository);
        TestUtils.injectObjects(orderController,"userRepository",userRepository);
        TestUtils.injectObjects(orderController,"orderRepository",orderRepository);
    }

    @Test
    public void testCreateUser(){
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Sarah");
        request.setPassword("17sara7");
        request.setConfirmPassword("17sara7");

        final ResponseEntity<User> response = userController.createUser(request);

        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatusCodeValue());

        User user = response.getBody();
        Assert.assertNotNull(user);
        Assert.assertEquals(0,user.getId());
        Assert.assertEquals("Sarah",user.getUsername());
    }

    @Test
    public void testFindUserByUserNameNotFound(){
        final ResponseEntity<User> response = userController.findByUserName("Lati");
        Assert.assertNull(response.getBody());
        Assert.assertEquals(404,response.getStatusCodeValue());
    }

    @Test
    public void testGetItemById(){
        when(itemRepository.findById(1L)).thenReturn(Optional.of(getFirstIem()));
        final ResponseEntity<Item> response = itemController.getItemById(1L);
        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatusCodeValue());
        Assert.assertEquals(response.getBody().getId() , getFirstIem().getId());
    }

    @Test
    public void TestGetAllItems(){
        when(itemRepository.findAll()).thenReturn(getAllItems());
        final ResponseEntity<List<Item>> response = itemController.getItems();
        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatusCodeValue());
        Assert.assertArrayEquals(getAllItems().toArray(), response.getBody().toArray());
    }

    @Test
    public void TestAddToCart(){
        when(userRepository.findByUsername("Sarah")).thenReturn(getUser());
        when(itemRepository.findById(1L)).thenReturn(Optional.of(getFirstIem()));
        final ResponseEntity<Cart> response = cartController.addTocart(getModifyCartRequest());
        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatusCodeValue());
        Cart cart= response.getBody();
        Assert.assertTrue(cart.getItems().contains(getFirstIem()));
    }

    @Test
    public void TestRemoveFromCart(){
        when(userRepository.findByUsername("Sarah")).thenReturn(getUser());
        when(itemRepository.findById(1L)).thenReturn(Optional.of(getFirstIem()));
        final ResponseEntity<Cart> response = cartController.removeFromcart(getModifyCartRequest());
        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatusCodeValue());
        Cart cart= response.getBody();
        Assert.assertFalse(cart.getItems().contains(getFirstIem()));
    }

    @Test
    public void TestSubmitOrder(){
        when(userRepository.findByUsername("Sarah")).thenReturn(getUser());
        final ResponseEntity<UserOrder> response = orderController.submit("Sarah");
        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatusCodeValue());
        UserOrder userOrder = response.getBody();
        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatusCodeValue());
        Assert.assertTrue(response.getBody().getItems().contains(getFirstIem()));
    }

    @Test
    public void TestSubmitOrderUserNotFound(){
        when(userRepository.findByUsername("Sarah")).thenReturn(getUser());
            final ResponseEntity<UserOrder> response = orderController.submit("Lati");
        Assert.assertNull(response.getBody());
        Assert.assertEquals(404,response.getStatusCodeValue());
    }

    @Test
    public void TestOrdersHistory(){
        User user = getUser();
        when(userRepository.findByUsername("Sarah")).thenReturn(user);
        when(orderRepository.findByUser(user)).thenReturn(getUserOrder());
        final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("Sarah");
        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatusCodeValue());
        Assert.assertFalse(response.getBody().isEmpty());
    }

    @Test
    public void TestOrdersHistoryUserNotFound(){
        User user = getUser();
        when(userRepository.findByUsername("Sarah")).thenReturn(user);
        when(orderRepository.findByUser(user)).thenReturn(getUserOrder());
        final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("Lati");
        Assert.assertNull(response.getBody());
        Assert.assertEquals(404,response.getStatusCodeValue());
    }
    private Item getFirstIem(){
        Item item = new Item();
        item.setId(1L);
        item.setName("Round Widget");
        item.setDescription("A widget that is round");
        item.setPrice(BigDecimal.valueOf(2.99));
        return item;
    }

    private List<Item> getAllItems(){
        Item item = new Item();
        item.setId(1L);
        item.setName("Round Widget");
        item.setDescription("A widget that is round");
        item.setPrice(BigDecimal.valueOf(2.99));
        Item item2 = new Item();
        item.setId(2L);
        item.setName("Square Widget");
        item.setDescription("'A widget that is square");
        item.setPrice(BigDecimal.valueOf(1.99));
        return Arrays.asList(item,item2);
    }
    private User getUser(){
        User user = new User();
        user.setId(1L);
        user.setUsername("Sarah");
        Cart cart= new Cart();
        cart.addItem(getFirstIem());
        user.setCart(cart);
        return user;
    }

    private ModifyCartRequest getModifyCartRequest() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("Sarah");
        request.setItemId(1L);
        request.setQuantity(1);
        return request;
    }

    private List<UserOrder> getUserOrder(){
        UserOrder userOrder= new UserOrder();
        User user = getUser();
        userOrder.setUser(user);
        userOrder.setItems(user.getCart().getItems());
        userOrder.setTotal(user.getCart().getTotal());
        userOrder.setId(1L);
        return Arrays.asList(userOrder);
    }
}

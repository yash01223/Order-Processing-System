import React, { createContext, useContext, useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import api from '../api/axios';

const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState(() => {
    const saved = localStorage.getItem('cart');
    return saved ? JSON.parse(saved) : [];
  });
  const [ordering, setOrdering] = useState(false);

  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart));
  }, [cart]);

  const addToCart = (product) => {
    let message = '';
    let type = 'success';

    setCart((prevCart) => {
      const existing = prevCart.find(item => item.product.id === product.id);
      if (existing) {
        if (existing.quantity >= product.stockCount) {
          message = `Only ${product.stockCount} in stock!`;
          type = 'warning';
          return prevCart;
        }
        message = `Increased ${product.name} quantity!`;
        return prevCart.map(item =>
          item.product.id === product.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      message = `Added ${product.name} to cart!`;
      return [...prevCart, { product, quantity: 1 }];
    });

    // Show toast AFTER state update logic
    if (message) {
      if (type === 'warning') toast.warning(message);
      else toast.success(message);
    }
  };

  const removeFromCart = (productId) => {
    setCart((prev) => prev.filter(item => item.product.id !== productId));
    toast.info('Item removed from cart');
  };

  const updateQuantity = (productId, newQuantity) => {
    if (newQuantity < 1) return removeFromCart(productId);
    setCart((prev) => prev.map(item =>
      item.product.id === productId ? { ...item, quantity: newQuantity } : item
    ));
  };

  const clearCart = () => setCart([]);

  /** Returns true on success, false on failure */
  const placeOrder = async () => {
    if (cart.length === 0) {
      toast.error('Your cart is empty.');
      return false;
    }
    setOrdering(true);
    try {
      const items = cart.map(i => ({ productId: i.product.id, quantity: i.quantity }));
      await api.post('/orders', { items });
      toast.success(' Order placed successfully!');
      clearCart();
      return true;
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to place order. Please try again.');
      return false;
    } finally {
      setOrdering(false);
    }
  };

  const cartCount = cart.reduce((acc, item) => acc + item.quantity, 0);
  const cartTotal = cart.reduce((acc, item) => acc + item.product.price * item.quantity, 0);

  return (
    <CartContext.Provider value={{
      cart,
      cartCount,
      cartTotal,
      ordering,
      addToCart,
      removeFromCart,
      updateQuantity,
      clearCart,
      placeOrder
    }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);

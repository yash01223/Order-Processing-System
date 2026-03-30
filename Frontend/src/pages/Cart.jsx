import React from 'react';
import { useCart } from '../context/CartContext';
import { useNavigate } from 'react-router-dom';
import {
  ShoppingBag, Trash2, Plus, Minus, Package,
  ArrowRight, ArrowLeft, Tag, Truck,
} from 'lucide-react';

const Cart = () => {
  const { cart, cartTotal, ordering, removeFromCart, updateQuantity, placeOrder } = useCart();
  const navigate = useNavigate();

  const handleCheckout = async () => {
    const success = await placeOrder();
    if (success) navigate('/orders');
  };

  if (cart.length === 0) {
    return (
      <div className="space-y-5">
        <div>
          <h1 className="page-title">Your Cart</h1>
          <p className="page-subtitle">Review your items before placing an order.</p>
        </div>
        <div className="empty-state py-20">
          <ShoppingBag size={56} className="empty-icon mx-auto mb-4" />
          <h3 className="text-white font-semibold text-lg mb-1">Your cart is empty</h3>
          <p className="text-muted text-sm mb-5">Add products from the catalogue to get started.</p>
          <button onClick={() => navigate('/products')} className="btn btn-primary gap-2">
            <ArrowLeft size={15} /> Browse Products
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title flex items-center gap-2">
            <ShoppingBag size={24} className="text-primary" />
            Your Cart
          </h1>
          <p className="page-subtitle">{cart.length} item{cart.length !== 1 ? 's' : ''} ready to order</p>
        </div>
        <button onClick={() => navigate('/products')} className="btn btn-outline btn-sm gap-2">
          <ArrowLeft size={14} /> Continue Shopping
        </button>
      </div>

      <div className="flex flex-col xl:flex-row gap-6">
        {/* Cart Items */}
        <div className="flex-1 space-y-3">
          {cart.map((item, idx) => (
            <div
              key={item.product.id}
              className="card flex gap-4 items-center animate-slide-up hover:border-primary/20 transition-all duration-200"
              style={{ animationDelay: `${idx * 50}ms` }}
            >
              {/* Product icon */}
              <div className="w-16 h-16 bg-dark rounded-xl flex items-center justify-center flex-shrink-0">
                <Package size={24} className="text-muted/40" />
              </div>

              {/* Info */}
              <div className="flex-1 min-w-0">
                <h3 className="font-semibold text-white text-sm leading-snug truncate">{item.product.name}</h3>
                <div className="flex items-center gap-2 mt-1">
                  <Tag size={11} className="text-muted" />
                  <span className="text-xs text-muted">{item.product.category}</span>
                </div>
                <p className="text-primary font-bold text-sm mt-1">₹{Number(item.product.price).toFixed(2)} each</p>
              </div>

              {/* Quantity Control */}
              <div className="flex items-center gap-2 bg-dark rounded-xl border border-surfaceBorder px-2 py-1.5">
                <button
                  onClick={() => updateQuantity(item.product.id, item.quantity - 1)}
                  className="w-6 h-6 rounded flex items-center justify-center text-muted hover:text-white hover:bg-surfaceHover transition-all"
                >
                  <Minus size={13} />
                </button>
                <span className="w-7 text-center text-sm font-semibold text-white">{item.quantity}</span>
                <button
                  onClick={() => updateQuantity(item.product.id, item.quantity + 1)}
                  disabled={item.quantity >= item.product.stockCount}
                  className="w-6 h-6 rounded flex items-center justify-center text-muted hover:text-white hover:bg-surfaceHover transition-all disabled:opacity-30"
                >
                  <Plus size={13} />
                </button>
              </div>

              {/* Line Total */}
              <div className="w-24 text-right flex-shrink-0">
                <p className="font-bold text-white">₹{(item.product.price * item.quantity).toFixed(2)}</p>
                <p className="text-xs text-muted">{item.quantity} × ₹{Number(item.product.price).toFixed(2)}</p>
              </div>

              {/* Remove */}
              <button
                onClick={() => removeFromCart(item.product.id)}
                className="btn btn-danger btn-icon p-2 flex-shrink-0"
                title="Remove item"
              >
                <Trash2 size={16} />
              </button>
            </div>
          ))}
        </div>

        {/* Order Summary Sidebar */}
        <div className="xl:w-80 flex-shrink-0">
          <div className="card sticky top-4">
            <h2 className="font-bold text-white mb-5 text-base border-b border-surfaceBorder pb-3">
              Order Summary
            </h2>

            <div className="space-y-3 mb-5">
              {cart.map(item => (
                <div key={item.product.id} className="flex justify-between text-sm">
                  <span className="text-muted truncate max-w-[160px]">{item.product.name} × {item.quantity}</span>
                  <span className="text-white font-medium flex-shrink-0 ml-2">
                    ₹{(item.product.price * item.quantity).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>

            <div className="border-t border-surfaceBorder pt-4 space-y-3 mb-5">
              <div className="flex justify-between text-sm text-muted">
                <span>Subtotal</span>
                <span>₹{cartTotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm text-muted">
                <span className="flex items-center gap-1.5"><Truck size={13} /> Shipping</span>
                <span className="text-emerald-400 font-medium">Free</span>
              </div>
              <div className="flex justify-between text-sm text-muted">
                <span>Tax (0%)</span>
                <span>₹0.00</span>
              </div>
            </div>

            <div className="border-t border-surfaceBorder pt-4 mb-5">
              <div className="flex justify-between items-center">
                <span className="font-semibold text-white">Total</span>
                <span className="text-2xl font-bold text-gradient-primary">
                  ₹{cartTotal.toFixed(2)}
                </span>
              </div>
            </div>

            <button
              onClick={handleCheckout}
              disabled={ordering}
              className="btn btn-primary w-full py-3 text-sm font-semibold gap-2"
            >
              {ordering ? (
                <><span className="spinner spinner-sm" /> Processing...</>
              ) : (
                <>Place Order <ArrowRight size={15} /></>
              )}
            </button>

            <p className="text-center text-[11px] text-muted mt-3">
              🔒 Secure checkout powered by OPS
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;

import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import api from '../api/axios';
import {
  ShoppingCart, Edit, Trash2, Plus, Search, X, Package,
  Tag, DollarSign, Layers, Save, AlertCircle,
} from 'lucide-react';
import { toast } from 'react-toastify';

/* ── Modal for Add / Edit Product ───────────────────────────────────────── */
const EMPTY_FORM = { name: '', description: '', price: '', stockCount: '', category: '' };

const ProductModal = ({ product, onClose, onSaved }) => {
  const isEdit = Boolean(product);
  const [form, setForm] = useState(
    isEdit
      ? { name: product.name, description: product.description, price: product.price, stockCount: product.stockCount, category: product.category }
      : EMPTY_FORM
  );
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Name is required';
    if (!form.price || isNaN(form.price) || Number(form.price) <= 0) e.price = 'Enter a valid price';
    if (!form.stockCount || isNaN(form.stockCount) || Number(form.stockCount) < 0) e.stockCount = 'Enter valid stock';
    if (!form.category.trim()) e.category = 'Category is required';
    return e;
  };

  const handleChange = (e) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    if (errors[e.target.name]) setErrors(prev => ({ ...prev, [e.target.name]: undefined }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setSaving(true);
    try {
      const payload = {
        name: form.name.trim(),
        description: form.description.trim(),
        price: Number(form.price),
        stockCount: Number(form.stockCount),
        category: form.category.trim(),
      };
      if (isEdit) {
        await api.put(`/products/${product.id}`, payload);
        toast.success('Product updated successfully!');
      } else {
        await api.post('/products', payload);
        toast.success('Product created successfully!');
      }
      onSaved();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save product');
    } finally {
      setSaving(false);
    }
  };

  const fields = [
    { name: 'name', label: 'Product Name', type: 'text', icon: <Package size={14} />, placeholder: 'e.g. Gaming Laptop' },
    { name: 'category', label: 'Category', type: 'text', icon: <Tag size={14} />, placeholder: 'e.g. Electronics' },
    { name: 'price', label: 'Price ($)', type: 'number', icon: <DollarSign size={14} />, placeholder: '0.00', min: '0', step: '0.01' },
    { name: 'stockCount', label: 'Stock Quantity', type: 'number', icon: <Layers size={14} />, placeholder: '0', min: '0' },
  ];

  return (
    <div className="modal-backdrop" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-box max-w-md" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center">
              {isEdit ? <Edit size={16} className="text-primary" /> : <Plus size={16} className="text-primary" />}
            </div>
            <h2 className="font-semibold text-white">{isEdit ? 'Edit Product' : 'New Product'}</h2>
          </div>
          <button onClick={onClose} className="btn btn-ghost btn-icon p-1" title="Close">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {/* 2-column grid for main fields */}
            <div className="grid grid-cols-2 gap-4">
              {fields.map(f => (
                <div key={f.name} className={`form-group ${f.name === 'name' ? 'col-span-2' : ''}`}>
                  <label className="label">{f.label}</label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted pointer-events-none">
                      {f.icon}
                    </span>
                    <input
                      name={f.name}
                      type={f.type}
                      placeholder={f.placeholder}
                      min={f.min}
                      step={f.step}
                      required
                      value={form[f.name]}
                      onChange={handleChange}
                      className={`input-field pl-9 ${errors[f.name] ? 'border-error focus:border-error focus:ring-error/30' : ''}`}
                    />
                  </div>
                  {errors[f.name] && (
                    <p className="text-error text-xs flex items-center gap-1 mt-1">
                      <AlertCircle size={10} /> {errors[f.name]}
                    </p>
                  )}
                </div>
              ))}

              {/* Description spans full width */}
              <div className="form-group col-span-2">
                <label className="label">Description</label>
                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  rows={3}
                  placeholder="Brief product description..."
                  className="input-field resize-none"
                />
              </div>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
            <button type="submit" disabled={saving} className="btn btn-primary gap-2">
              {saving ? <span className="spinner spinner-sm" /> : <Save size={15} />}
              {isEdit ? 'Save Changes' : 'Create Product'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

/* ── Delete Confirm Dialog ───────────────────────────────────────────────── */
const DeleteDialog = ({ product, onClose, onConfirm }) => (
  <div className="modal-backdrop" onClick={(e) => e.target === e.currentTarget && onClose()}>
    <div className="modal-box max-w-sm" onClick={e => e.stopPropagation()}>
      <div className="modal-header">
        <h2 className="font-semibold text-white">Delete Product</h2>
        <button onClick={onClose} className="btn btn-ghost btn-icon p-1"><X size={18} /></button>
      </div>
      <div className="modal-body">
        <div className="flex items-center gap-3 p-4 bg-error/10 border border-error/20 rounded-lg">
          <AlertCircle size={20} className="text-error flex-shrink-0" />
          <p className="text-sm text-white">
            Are you sure you want to delete <strong>"{product.name}"</strong>? This action cannot be undone.
          </p>
        </div>
      </div>
      <div className="modal-footer">
        <button onClick={onClose} className="btn btn-outline">Cancel</button>
        <button onClick={onConfirm} className="btn btn-danger">
          <Trash2 size={15} /> Delete
        </button>
      </div>
    </div>
  </div>
);

/* ── Products Page ────────────────────────────────────────────────────────── */
const CATEGORIES = ['All', 'Electronics', 'Clothing', 'Home & Kitchen', 'Sports', 'Books', 'Toys', 'Other'];

const Products = () => {
  const { isAdmin } = useAuth();
  const { addToCart } = useCart();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [showModal, setShowModal] = useState(false);
  const [editProduct, setEditProduct] = useState(null); // null = add mode
  const [deleteTarget, setDeleteTarget] = useState(null);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page, size: 12 });
      if (search) params.set('name', search);
      if (category) params.set('category', category);
      const { data } = await api.get(`/products?${params}`);
      setProducts(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (e) {
      toast.error('Failed to load products');
    } finally {
      setLoading(false);
    }
  }, [page, search, category]);

  useEffect(() => {
    const delay = setTimeout(fetchProducts, 300);
    return () => clearTimeout(delay);
  }, [fetchProducts]);

  const handleDelete = async () => {
    try {
      await api.delete(`/products/${deleteTarget.id}`);
      toast.success('Product deleted');
      setDeleteTarget(null);
      fetchProducts();
    } catch (e) {
      toast.error('Failed to delete product');
    }
  };

  const openAdd = () => { setEditProduct(null); setShowModal(true); };
  const openEdit = (p) => { setEditProduct(p); setShowModal(true); };
  const closeModal = () => { setShowModal(false); setEditProduct(null); };
  const handleSaved = () => { closeModal(); fetchProducts(); };

  const handleSearch = (e) => { setSearch(e.target.value); setPage(0); };
  const handleCategory = (cat) => { setCategory(cat === 'All' ? '' : cat); setPage(0); };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title">Product Catalogue</h1>
          <p className="page-subtitle">
            {isAdmin ? 'Manage your product listings.' : 'Browse and add products to your cart.'}
          </p>
        </div>
        {isAdmin && (
          <button onClick={openAdd} className="btn btn-primary gap-2 self-start sm:self-auto">
            <Plus size={16} /> Add Product
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        {/* Search */}
        <div className="relative flex-1 max-w-sm">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted pointer-events-none" />
          <input
            type="text"
            value={search}
            onChange={handleSearch}
            placeholder="Search products..."
            className="input-field pl-9 pr-8"
          />
          {search && (
            <button onClick={() => setSearch('')} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted hover:text-white">
              <X size={14} />
            </button>
          )}
        </div>

        {/* Category Pills */}
        <div className="flex flex-wrap gap-2">
          {CATEGORIES.map(cat => (
            <button
              key={cat}
              onClick={() => handleCategory(cat)}
              className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all border ${
                (cat === 'All' && !category) || category === cat
                  ? 'bg-primary text-dark border-primary'
                  : 'border-surfaceBorder text-muted hover:text-white hover:border-primary/40'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Grid */}
      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-32 bg-surfaceHover rounded-lg mb-4" />
              <div className="h-4 bg-surfaceHover rounded mb-2 w-3/4" />
              <div className="h-3 bg-surfaceHover rounded mb-4 w-1/2" />
              <div className="h-6 bg-surfaceHover rounded mb-2 w-1/3" />
              <div className="h-8 bg-surfaceHover rounded" />
            </div>
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="empty-state">
          <Package size={48} className="empty-icon mx-auto" />
          <h3 className="text-white font-medium mb-1">No products found</h3>
          <p className="text-muted text-sm">Try adjusting your search or category filter.</p>
          {isAdmin && (
            <button onClick={openAdd} className="btn btn-primary mt-4 gap-2">
              <Plus size={16} /> Add First Product
            </button>
          )}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-4 gap-4">
            {products.map((p, idx) => (
              <div
                key={p.id}
                className="card flex flex-col hover:border-primary/25 hover:-translate-y-0.5 hover:shadow-glow-primary transition-all duration-200 animate-fade-in"
                style={{ animationDelay: `${idx * 40}ms` }}
              >
                {/* Product image placeholder with category badge */}
                <div className="relative h-36 bg-gradient-to-br from-surfaceHover to-dark rounded-lg mb-4 flex items-center justify-center overflow-hidden">
                  <Package size={40} className="text-muted/30" />
                  <div className="absolute top-2 right-2">
                    <span className="badge bg-surfaceBorder/80 text-muted text-[10px]">{p.category}</span>
                  </div>
                  {!p.inStock && (
                    <div className="absolute inset-0 bg-dark/60 flex items-center justify-center rounded-lg">
                      <span className="badge status-cancelled">Out of Stock</span>
                    </div>
                  )}
                </div>

                {/* Info */}
                <div className="flex-1 flex flex-col">
                  <h3 className="font-semibold text-white text-sm leading-snug mb-1 line-clamp-2">{p.name}</h3>
                  <p className="text-xs text-muted line-clamp-2 mb-3 leading-relaxed flex-1">{p.description}</p>

                  <div className="flex items-center justify-between mb-3">
                    <span className="text-xl font-bold text-gradient-primary">
                      ${Number(p.price).toFixed(2)}
                    </span>
                    <span className={`text-xs px-2 py-0.5 rounded-full ${p.inStock ? 'bg-emerald-500/10 text-emerald-400' : 'bg-red-500/10 text-red-400'}`}>
                      {p.inStock ? `${p.stockCount} left` : 'Out of stock'}
                    </span>
                  </div>

                  {/* Actions */}
                  {isAdmin ? (
                    <div className="flex gap-2 mt-auto">
                      <button
                        className="btn btn-outline flex-1 btn-sm gap-1"
                        onClick={() => openEdit(p)}
                      >
                        <Edit size={13} /> Edit
                      </button>
                      <button
                        className="btn btn-danger btn-sm gap-1 flex-1"
                        onClick={() => setDeleteTarget(p)}
                      >
                        <Trash2 size={13} /> Delete
                      </button>
                    </div>
                  ) : (
                    <button
                      className="btn btn-primary w-full btn-sm gap-2 mt-auto"
                      onClick={() => addToCart(p)}
                      disabled={!p.inStock}
                    >
                      <ShoppingCart size={14} />
                      {p.inStock ? 'Add to Cart' : 'Out of Stock'}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-2">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="btn btn-outline btn-sm"
              >
                ← Prev
              </button>
              <div className="flex gap-1">
                {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
                  const p = totalPages <= 7 ? i : i; // simplified
                  return (
                    <button
                      key={i}
                      onClick={() => setPage(i)}
                      className={`w-8 h-8 rounded-lg text-sm font-medium transition-all ${
                        page === i ? 'bg-primary text-dark' : 'btn btn-ghost'
                      }`}
                    >
                      {i + 1}
                    </button>
                  );
                })}
              </div>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="btn btn-outline btn-sm"
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}

      {/* Modals */}
      {showModal && (
        <ProductModal
          product={editProduct}
          onClose={closeModal}
          onSaved={handleSaved}
        />
      )}
      {deleteTarget && (
        <DeleteDialog
          product={deleteTarget}
          onClose={() => setDeleteTarget(null)}
          onConfirm={handleDelete}
        />
      )}
    </div>
  );
};

export default Products;

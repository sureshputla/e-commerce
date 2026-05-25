const products = [
  { id: 1, name: 'Soft Dry Diapers - M', category: 'Diapers', brand: 'TinyCare', price: 799 },
  { id: 2, name: 'Anti-Colic Feeding Bottle', category: 'Bottles', brand: 'MilkNest', price: 349 },
  { id: 3, name: 'Stacking Ring Toy', category: 'Toys', brand: 'PlayBud', price: 499 },
  { id: 4, name: 'Battery Jeep Ride-On', category: 'Vehicle Toys', brand: 'ZoomKid', price: 8999 },
  { id: 5, name: 'Rocking Cradle', category: 'Cradles', brand: 'SleepLeaf', price: 4999 },
  { id: 6, name: 'Cotton Baby Cloth Set', category: 'Cloths', brand: 'PureWrap', price: 699 },
  { id: 7, name: 'Training Pants Diapers', category: 'Diapers', brand: 'PureWrap', price: 649 },
  { id: 8, name: 'Sipper Bottle', category: 'Bottles', brand: 'TinyCare', price: 279 }
];

const state = {
  category: 'all',
  brand: 'all',
  maxPrice: '',
  search: '',
  cart: new Map(),
  wishlist: new Set()
};

const categoryFilter = document.getElementById('categoryFilter');
const brandFilter = document.getElementById('brandFilter');
const priceFilter = document.getElementById('priceFilter');
const searchFilter = document.getElementById('searchFilter');
const productList = document.getElementById('productList');
const wishlistList = document.getElementById('wishlistList');
const cartList = document.getElementById('cartList');
const cartTotal = document.getElementById('cartTotal');
const paymentMethod = document.getElementById('paymentMethod');
const paymentStatus = document.getElementById('paymentStatus');
const checkoutButton = document.getElementById('checkoutButton');
const paymentMethodLabels = {
  card: 'Card',
  upi: 'UPI',
  cod: 'Cash on Delivery'
};

function renderFilterOptions(select, values) {
  values.forEach((value) => {
    const option = document.createElement('option');
    option.value = value;
    option.textContent = value;
    select.appendChild(option);
  });
}

function getFilteredProducts() {
  return products.filter((product) => {
    const isCategoryMatch = state.category === 'all' || product.category === state.category;
    const isBrandMatch = state.brand === 'all' || product.brand === state.brand;
    const isPriceMatch = !state.maxPrice || product.price <= Number(state.maxPrice);
    const isSearchMatch =
      !state.search || product.name.toLowerCase().includes(state.search.toLowerCase());
    return isCategoryMatch && isBrandMatch && isPriceMatch && isSearchMatch;
  });
}

function renderProducts() {
  productList.textContent = '';
  const filteredProducts = getFilteredProducts();

  if (!filteredProducts.length) {
    const emptyState = document.createElement('p');
    emptyState.textContent = 'No products match the selected filters.';
    productList.appendChild(emptyState);
    return;
  }

  filteredProducts.forEach((product) => {
    const card = document.createElement('article');
    card.className = 'product';

    const title = document.createElement('h3');
    title.textContent = product.name;

    const details = document.createElement('p');
    details.textContent = `${product.category} | ${product.brand}`;

    const price = document.createElement('p');
    price.textContent = `₹${product.price}`;

    const actions = document.createElement('div');
    actions.className = 'actions';

    const wishlistButton = document.createElement('button');
    wishlistButton.className = 'secondary';
    wishlistButton.textContent = state.wishlist.has(product.id)
      ? 'Remove from Wishlist'
      : 'Add to Wishlist';
    wishlistButton.addEventListener('click', () => {
      if (state.wishlist.has(product.id)) {
        state.wishlist.delete(product.id);
      } else {
        state.wishlist.add(product.id);
      }
      renderWishlist();
      renderProducts();
    });

    const cartButton = document.createElement('button');
    cartButton.textContent = 'Add to Cart';
    cartButton.addEventListener('click', () => {
      const currentQty = state.cart.get(product.id) || 0;
      state.cart.set(product.id, currentQty + 1);
      renderCart();
    });

    actions.append(wishlistButton, cartButton);
    card.append(title, details, price, actions);
    productList.appendChild(card);
  });
}

function renderWishlist() {
  wishlistList.textContent = '';

  if (!state.wishlist.size) {
    const li = document.createElement('li');
    li.textContent = 'Wishlist is empty';
    wishlistList.appendChild(li);
    return;
  }

  state.wishlist.forEach((id) => {
    const product = products.find((item) => item.id === id);
    if (!product) {
      return;
    }
    const li = document.createElement('li');
    li.textContent = `${product.name} (₹${product.price})`;
    wishlistList.appendChild(li);
  });
}

function renderCart() {
  cartList.textContent = '';
  let total = 0;

  if (!state.cart.size) {
    const li = document.createElement('li');
    li.textContent = 'Cart is empty';
    cartList.appendChild(li);
    cartTotal.textContent = 'Total: ₹0';
    return;
  }

  state.cart.forEach((qty, id) => {
    const product = products.find((item) => item.id === id);
    if (!product) {
      return;
    }
    total += product.price * qty;

    const li = document.createElement('li');
    li.textContent = `${product.name} x${qty} — ₹${product.price * qty}`;

    const removeButton = document.createElement('button');
    removeButton.className = 'secondary';
    removeButton.textContent = 'Remove';
    removeButton.addEventListener('click', () => {
      state.cart.delete(id);
      renderCart();
    });

    li.appendChild(removeButton);
    cartList.appendChild(li);
  });

  cartTotal.textContent = `Total: ₹${total}`;
}

function checkout() {
  if (!state.cart.size) {
    paymentStatus.textContent = 'Add products to the cart before payment.';
    return;
  }

  const method = paymentMethod.value;
  const total = [...state.cart.entries()].reduce((sum, [id, qty]) => {
    const product = products.find((item) => item.id === id);
    return sum + (product ? product.price * qty : 0);
  }, 0);

  const methodLabel = paymentMethodLabels[method] || method;
  paymentStatus.textContent = `Payment successful via ${methodLabel} for ₹${total}.`;
  state.cart.clear();
  renderCart();
}

function initialize() {
  const categories = [...new Set(products.map((product) => product.category))];
  const brands = [...new Set(products.map((product) => product.brand))];

  renderFilterOptions(categoryFilter, categories);
  renderFilterOptions(brandFilter, brands);

  categoryFilter.addEventListener('change', (event) => {
    state.category = event.target.value;
    renderProducts();
  });

  brandFilter.addEventListener('change', (event) => {
    state.brand = event.target.value;
    renderProducts();
  });

  priceFilter.addEventListener('input', (event) => {
    state.maxPrice = event.target.value.trim();
    renderProducts();
  });

  searchFilter.addEventListener('input', (event) => {
    state.search = event.target.value.trim();
    renderProducts();
  });

  checkoutButton.addEventListener('click', checkout);

  renderProducts();
  renderWishlist();
  renderCart();
}

initialize();

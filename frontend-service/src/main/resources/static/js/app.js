// API Gateway base URL injected by Thymeleaf (see index.html)
const BASE = window.API_GATEWAY || 'http://localhost:8080';

const state = {
    category: 'all',
    brand: 'all',
    maxPrice: '',
    search: '',
    wishlistIds: new Set()
};

const categoryFilter = document.getElementById('categoryFilter');
const brandFilter    = document.getElementById('brandFilter');
const priceFilter    = document.getElementById('priceFilter');
const searchFilter   = document.getElementById('searchFilter');
const productList    = document.getElementById('productList');
const wishlistList   = document.getElementById('wishlistList');
const cartList       = document.getElementById('cartList');
const cartTotal      = document.getElementById('cartTotal');
const paymentMethod  = document.getElementById('paymentMethod');
const paymentStatus  = document.getElementById('paymentStatus');
const checkoutButton = document.getElementById('checkoutButton');

async function fetchJson(path, options = {}) {
    const response = await fetch(`${BASE}${path}`, options);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || 'Request failed');
    }
    return data;
}

function renderFilterOptions(select, values) {
    values.forEach((value) => {
        const option = document.createElement('option');
        option.value = value;
        option.textContent = value;
        select.appendChild(option);
    });
}

async function renderProducts() {
    const params = new URLSearchParams();
    if (state.category !== 'all') params.set('category', state.category);
    if (state.brand !== 'all')    params.set('brand', state.brand);
    if (state.maxPrice)           params.set('maxPrice', state.maxPrice);
    if (state.search)             params.set('search', state.search);

    const products = await fetchJson(`/api/products?${params.toString()}`);
    productList.textContent = '';

    if (!products.length) {
        const emptyState = document.createElement('p');
        emptyState.textContent = 'No products match the selected filters.';
        productList.appendChild(emptyState);
        return;
    }

    products.forEach((product) => {
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
        wishlistButton.textContent = state.wishlistIds.has(product.id)
            ? 'Remove from Wishlist'
            : 'Add to Wishlist';
        wishlistButton.addEventListener('click', async () => {
            if (state.wishlistIds.has(product.id)) {
                await fetchJson(`/api/wishlist/${product.id}`, { method: 'DELETE' });
            } else {
                await fetchJson(`/api/wishlist/${product.id}`, { method: 'POST' });
            }
            await renderWishlist();
            await renderProducts();
        });

        const cartButton = document.createElement('button');
        cartButton.textContent = 'Add to Cart';
        cartButton.addEventListener('click', async () => {
            await fetchJson(`/api/cart/${product.id}`, { method: 'POST' });
            await renderCart();
        });

        actions.append(wishlistButton, cartButton);
        card.append(title, details, price, actions);
        productList.appendChild(card);
    });
}

async function renderWishlist() {
    wishlistList.textContent = '';
    const wishlist = await fetchJson('/api/wishlist');
    state.wishlistIds = new Set(wishlist.map((product) => product.id));

    if (!wishlist.length) {
        const li = document.createElement('li');
        li.textContent = 'Wishlist is empty';
        wishlistList.appendChild(li);
        return;
    }

    wishlist.forEach((product) => {
        const li = document.createElement('li');
        li.textContent = `${product.name} (₹${product.price})`;
        wishlistList.appendChild(li);
    });
}

async function renderCart() {
    cartList.textContent = '';
    const cart = await fetchJson('/api/cart');

    if (!cart.items || !cart.items.length) {
        const li = document.createElement('li');
        li.textContent = 'Cart is empty';
        cartList.appendChild(li);
        cartTotal.textContent = 'Total: ₹0';
        return;
    }

    cart.items.forEach((item) => {
        const li = document.createElement('li');
        li.className = 'cart-item';
        li.textContent = `${item.product.name} x${item.quantity} — ₹${item.subtotal}`;

        const removeButton = document.createElement('button');
        removeButton.className = 'secondary';
        removeButton.textContent = 'Remove';
        removeButton.addEventListener('click', async () => {
            await fetchJson(`/api/cart/${item.product.id}`, { method: 'DELETE' });
            await renderCart();
        });

        li.appendChild(removeButton);
        cartList.appendChild(li);
    });

    cartTotal.textContent = `Total: ₹${cart.total}`;
}

async function checkout() {
    try {
        checkoutButton.disabled = true;
        paymentStatus.textContent = 'Processing...';
        const method = paymentMethod.value;
        const result = await fetchJson(`/api/checkout?paymentMethod=${encodeURIComponent(method)}`, {
            method: 'POST'
        });
        paymentStatus.textContent = result.message;
        await renderCart();
    } catch (error) {
        paymentStatus.textContent = error.message;
    } finally {
        checkoutButton.disabled = false;
    }
}

async function initialize() {
    const filters = await fetchJson('/api/filters');
    renderFilterOptions(categoryFilter, filters.categories);
    renderFilterOptions(brandFilter, filters.brands);

    categoryFilter.addEventListener('change', async (event) => {
        state.category = event.target.value;
        await renderProducts();
    });

    brandFilter.addEventListener('change', async (event) => {
        state.brand = event.target.value;
        await renderProducts();
    });

    priceFilter.addEventListener('input', async (event) => {
        state.maxPrice = event.target.value.trim();
        await renderProducts();
    });

    searchFilter.addEventListener('input', async (event) => {
        state.search = event.target.value.trim();
        await renderProducts();
    });

    checkoutButton.addEventListener('click', checkout);

    await renderWishlist();
    await renderCart();
    await renderProducts();
}

initialize().catch((error) => {
    paymentStatus.textContent = `Unable to load store: ${error.message}`;
});


# Order Processing System 🚀

A full-stack, professional order management application built with **Spring Boot** and **React**. This system allows customers to browse products and place orders, while providing administrators with a powerful dashboard to manage inventory, users, and order statuses.

## 🌟 Key Features

### **Security & Roles**
- **JWT Authentication:** Secure login and registration using JSON Web Tokens.
- **Role-Based Access Control (RBAC):** Separate interfaces and permissions for **Admins** and **Customers**.
- **Protected Routes:** Frontend and backend routes are secured based on user authority.

### **Admin Capabilities**
- **Inventory Management:** Full CRUD (Create, Read, Update, Delete) operations for products.
- **Soft Deletion:** Products can be "deleted" without breaking historical order data (stored in DB but hidden from shop).
- **Order Tracking:** Monitor all customer orders and update their fulfillment status.
- **User Overview:** View and manage registered users.

### **Customer Features**
- **Product Catalog:** Browse products with real-time stock status and categories.
- **Order Placement:** Seamlessly add items to cart and checkout (Transaction-safe).
- **Order History:** View personal past orders and their current status.

---

## 🛠️ Tech Stack

| Layer | Technologies |
|--- |--- |
| **Backend** | Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security |
| **Frontend** | React 19, Vite, Tailwind CSS, Axios |
| **Database** | PostgreSQL |
| **Auth** | JWT (jjwt 0.13.0) |

---

## 🚀 Getting Started

### **Prerequisites**
- Java 17+
- SpringBoot (3.x)
- PostgreSQL Database
- Maven

### **1. Backend Setup**
1. Navigate to the `Backend` directory.
2. Update `src/main/resources/application.properties` with your PostgreSQL credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/orderdb
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   *The server will start on [http://localhost:8085/api](http://localhost:8085/api)*

### **2. Frontend Setup**
1. Navigate to the `Frontend` directory.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   *The UI will be available at [http://localhost:5173](http://localhost:5173)*

---

## 📂 Project Structure

```text
├── Backend/                # Spring Boot Application
│   ├── src/main/java       # Source code (Controllers, Services, Entities)
│   ├── src/main/resources  # Configuration & SQL
│   └── pom.xml             # Maven dependencies
├── Frontend/               # React Application (Vite)
│   ├── src/components      # Reusable UI components
│   ├── src/api             # Axios configuration
│   ├── src/App.jsx         # Main routing
│   └── tailwind.config.js  # Styling configuration
└── README.md               # You are here
```

---

## 🛡️ License
Distributed under the MIT License. See `LICENSE` for more information.


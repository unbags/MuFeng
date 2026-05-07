# Login & Register Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为"暖光食刻"实现管理员登录与注册功能 — 数据库新增 users 表，后端改为 BCrypt + 数据库认证，前端新增登录/注册页 + token 管理 + 路由守卫。

**Architecture:** 前端 LoginView/RegisterView 通过 `api/auth.js` 调用 `POST /api/auth/login|register`，后端 AuthController → UserService（BCrypt 加密 + Snowflake ID）→ UserMapper → MySQL `users` 表。前端 `router.beforeEach` 守卫保护管理路由，`api/index.js` 自动注入 Bearer token。

**Tech Stack:** Vue 3 (Composition API), Vue Router 4, Spring Boot 2.7, MyBatis-Plus 3.5, Spring Security, JWT (jjwt 0.9.1), BCrypt, MySQL 8

---

### Task 1: 数据库 — 新增 users 表

**Files:**
- Modify: `backend/src/main/resources/schema.sql`

- [ ] **Step 1: 在 schema.sql 末尾追加 CREATE TABLE 语句**

在 `schema.sql` 文件末尾追加以下内容：

```sql
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(64)  NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users (username);
```

- [ ] **Step 2: 在数据库中执行建表**

Run: 连接到 MySQL `ordering_demo` 数据库，执行 `schema.sql` 中的 `CREATE TABLE IF NOT EXISTS users` 语句。

或者重新运行整个 schema.sql：
```
mysql -u root -p ordering_demo < backend/src/main/resources/schema.sql
```

Expected: 数据库中新增 `users` 表，包含 id, username, password_hash, display_name, created_at, updated_at 列。

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/schema.sql
git commit -m "feat: add users table for authentication"
```

---

### Task 2: 后端 — 创建 User 领域实体

**Files:**
- Create: `backend/src/main/java/com/example/ordering/domain/User.java`

- [ ] **Step 1: 创建 User.java**

遵循项目现有模式（getter/setter，@TableName，@TableId）：

```java
package com.example.ordering.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("users")
public class User {

    @TableId
    private Long id;

    private String username;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("display_name")
    private String displayName;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/com/example/ordering/domain/User.java
git commit -m "feat: add User domain entity"
```

---

### Task 3: 后端 — 创建 UserMapper 接口

**Files:**
- Create: `backend/src/main/java/com/example/ordering/mapper/UserMapper.java`

- [ ] **Step 1: 创建 UserMapper.java**

```java
package com.example.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ordering.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/com/example/ordering/mapper/UserMapper.java
git commit -m "feat: add UserMapper interface"
```

---

### Task 4: 后端 — 创建 RegisterRequest 和 AuthStatusResponse DTO

**Files:**
- Create: `backend/src/main/java/com/example/ordering/dto/RegisterRequest.java`
- Create: `backend/src/main/java/com/example/ordering/dto/AuthStatusResponse.java`

- [ ] **Step 1: 创建 RegisterRequest.java**

```java
package com.example.ordering.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度需在3-64位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 128, message = "密码长度需在6-128位之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;

    @NotBlank(message = "显示名称不能为空")
    @Size(max = 64, message = "显示名称不能超过64位")
    private String displayName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
```

- [ ] **Step 2: 创建 AuthStatusResponse.java**

```java
package com.example.ordering.dto;

public class AuthStatusResponse {

    private final boolean hasAdmin;

    public AuthStatusResponse(boolean hasAdmin) {
        this.hasAdmin = hasAdmin;
    }

    public boolean isHasAdmin() {
        return hasAdmin;
    }
}
```

- [ ] **Step 3: 更新 LoginResponse.java 加入 username 和 displayName**

修改 `backend/src/main/java/com/example/ordering/dto/LoginResponse.java`，在原有 token 字段基础上增加 username 和 displayName：

```java
package com.example.ordering.dto;

public class LoginResponse {

    private String token;
    private String username;
    private String displayName;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, String displayName) {
        this.token = token;
        this.username = username;
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/example/ordering/dto/RegisterRequest.java backend/src/main/java/com/example/ordering/dto/AuthStatusResponse.java backend/src/main/java/com/example/ordering/dto/LoginResponse.java
git commit -m "feat: add RegisterRequest, AuthStatusResponse DTOs and extend LoginResponse"
```

---

### Task 5: 后端 — 创建 UserService

**Files:**
- Create: `backend/src/main/java/com/example/ordering/service/UserService.java`
- Create: `backend/src/main/java/com/example/ordering/service/impl/UserServiceImpl.java`

- [ ] **Step 1: 创建 UserService 接口**

```java
package com.example.ordering.service;

import com.example.ordering.domain.User;

public interface UserService {

    /** 注册新管理员。若系统已有管理员则抛出异常。返回生成的用户。 */
    User register(String username, String rawPassword, String displayName);

    /** 验证登录凭据，成功返回用户，失败返回 null */
    User authenticate(String username, String rawPassword);

    /** 系统是否已有管理员 */
    boolean hasAdmin();

    /** 根据用户名查找用户 */
    User findByUsername(String username);
}
```

- [ ] **Step 2: 创建 UserServiceImpl 实现类**

```java
package com.example.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ordering.domain.User;
import com.example.ordering.mapper.UserMapper;
import com.example.ordering.service.SnowflakeIdGenerator;
import com.example.ordering.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SnowflakeIdGenerator idGenerator;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, SnowflakeIdGenerator idGenerator) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
    }

    @Override
    public User register(String username, String rawPassword, String displayName) {
        if (hasAdmin()) {
            throw new IllegalStateException("系统已初始化，无法重复注册");
        }
        if (findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setId(idGenerator.nextId());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(displayName);

        userMapper.insert(user);
        return user;
    }

    @Override
    public User authenticate(String username, String rawPassword) {
        User user = findByUsername(username);
        if (user == null) {
            return null;
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    @Override
    public boolean hasAdmin() {
        return userMapper.selectCount(null) > 0;
    }

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/example/ordering/service/UserService.java backend/src/main/java/com/example/ordering/service/impl/UserServiceImpl.java
git commit -m "feat: add UserService with BCrypt auth and registration"
```

---

### Task 6: 后端 — 在 SecurityConfig 中添加 PasswordEncoder Bean

**Files:**
- Modify: `backend/src/main/java/com/example/ordering/config/SecurityConfig.java`

- [ ] **Step 1: 在 SecurityConfig 中添加 PasswordEncoder @Bean**

在 `SecurityConfig.java` 末尾（类闭括号之前）添加：

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

同时在文件顶部 import 区域新增：
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/com/example/ordering/config/SecurityConfig.java
git commit -m "feat: add PasswordEncoder bean to SecurityConfig"
```

---

### Task 7: 后端 — 重写 AuthController

**Files:**
- Modify: `backend/src/main/java/com/example/ordering/controller/AuthController.java`

- [ ] **Step 1: 用完整实现替换 AuthController.java**

将整个文件替换为：

```java
package com.example.ordering.controller;

import com.example.ordering.domain.User;
import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.AuthStatusResponse;
import com.example.ordering.dto.LoginRequest;
import com.example.ordering.dto.LoginResponse;
import com.example.ordering.dto.RegisterRequest;
import com.example.ordering.security.JwtTokenUtil;
import com.example.ordering.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public AuthController(JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error(401, "用户名或密码错误"));
        }
        String token = jwtTokenUtil.generateToken(user.getUsername());
        LoginResponse resp = new LoginResponse(token, user.getUsername(), user.getDisplayName());
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(
            request.getUsername(),
            request.getPassword(),
            request.getDisplayName()
        );
        String token = jwtTokenUtil.generateToken(user.getUsername());
        LoginResponse resp = new LoginResponse(token, user.getUsername(), user.getDisplayName());
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/status")
    public ApiResponse<AuthStatusResponse> status() {
        return ApiResponse.success(new AuthStatusResponse(userService.hasAdmin()));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/com/example/ordering/controller/AuthController.java
git commit -m "feat: rewrite AuthController with DB-backed login, register, and status endpoints"
```

---

### Task 8: 后端 — 更新 SecurityConfig 路由规则

**Files:**
- Modify: `backend/src/main/java/com/example/ordering/config/SecurityConfig.java`

- [ ] **Step 1: 显式放行 /api/auth/register 和 /api/auth/status**

在 `SecurityConfig.java` 的 `configure(HttpSecurity http)` 方法中，`.antMatchers("/api/auth/**").permitAll()` 已经覆盖了 `/api/auth/register` 和 `/api/auth/status`，因为它们都在 `/api/auth/**` 路径下。

**确认** `.antMatchers("/api/auth/**").permitAll()` 这一行已存在（确实存在，无需修改）。此步跳过代码变更。

- [ ] **Step 2: 无需额外修改，直接 Commit（空提交跳过）**

确认 SecurityConfig 无需改动。继续下一个任务。

---

### Task 9: 后端 — 移除 application.yml 中的硬编码凭据

**Files:**
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 注释或移除 app.admin.username 和 app.admin.password**

将 `application.yml` 中的：

```yaml
app:
  admin:
    username: ${ADMIN_USERNAME:admin}
    password: ${ADMIN_PASSWORD:admin123}
```

改为注释掉（保留 cwd 下的其他配置）：

```yaml
app:
  # admin credentials are now stored in the users database table
  # username: ${ADMIN_USERNAME:admin}
  # password: ${ADMIN_PASSWORD:admin123}
```

**注意**：只注释 username 和 password 两行，保留 `app:` 下的其他配置（cors, high-concurrency, storage）。

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/resources/application.yml
git commit -m "feat: remove hardcoded admin credentials, now DB-backed"
```

---

### Task 10: 后端 — 更新 GlobalExceptionHandler 处理 401

**Files:**
- Modify: `backend/src/main/java/com/example/ordering/config/GlobalExceptionHandler.java`

- [ ] **Step 1: 无需修改**

AuthController 中登录失败已直接用 `ResponseEntity.status(401)` 返回，不走异常处理器。注册的"系统已初始化"抛 `IllegalStateException` → 503（已有处理），"用户名已存在"抛 `IllegalArgumentException` → 400（已有处理）。无需改动。

此任务跳过。

---

### Task 11: 前端 — 创建 API 层 auth.js

**Files:**
- Create: `frontend/src/api/auth.js`

- [ ] **Step 1: 创建 auth.js**

```javascript
import { request } from './index.js'

export function login(username, password) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function register(username, password, displayName) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password, displayName }),
  })
}

export function fetchAuthStatus() {
  return request('/auth/status')
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/api/auth.js
git commit -m "feat: add auth API functions"
```

---

### Task 12: 前端 — 修改 api/index.js 自动注入 token

**Files:**
- Modify: `frontend/src/api/index.js`

- [ ] **Step 1: 在 request() 函数中自动添加 Authorization header**

修改 `request()` 函数，在 fetch 调用前从 localStorage 读取 token 并附加到 headers：

在 `frontend/src/api/index.js` 中，将 `request` 函数修改为：

```javascript
export async function request(path, options = {}) {
  const token = localStorage.getItem('token')

  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData

  const headers = isFormData
    ? { ...(options.headers ?? {}) }
    : {
        'Content-Type': 'application/json',
        ...(options.headers ?? {}),
      }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    headers,
    ...options,
  })

  if (!response.ok) {
    let message = '请求失败'
    try {
      const errorBody = await response.json()
      message = errorBody.message || message
    } catch {
      message = `${message} (${response.status})`
    }
    throw new Error(message)
  }

  if (response.status === 204) return null

  const text = await response.text()
  if (!text) return null

  const body = JSON.parse(text)

  if (body && typeof body === 'object' && 'code' in body && 'data' in body) {
    if (body.code !== 200) {
      throw new Error(body.message || '请求失败')
    }
    return body.data
  }

  return body
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/api/index.js
git commit -m "feat: auto-attach Bearer token to API requests"
```

---

### Task 13: 前端 — 创建 useAuth composable

**Files:**
- Create: `frontend/src/composables/useAuth.js`

- [ ] **Step 1: 创建 useAuth.js**

```javascript
import { reactive } from 'vue'
import { login as apiLogin, register as apiRegister, fetchAuthStatus } from '../api/auth.js'

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

function loadPersisted() {
  const token = localStorage.getItem(TOKEN_KEY)
  let user = null
  try {
    const raw = localStorage.getItem(USER_KEY)
    if (raw) user = JSON.parse(raw)
  } catch {
    user = null
  }
  return { token, user }
}

const state = reactive({
  token: null,
  user: null,
  loading: false,
  error: null,
})

// 启动时从 localStorage 恢复
const persisted = loadPersisted()
state.token = persisted.token
state.user = persisted.user

function persist(token, user) {
  state.token = token
  state.user = user
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }
}

export function useAuth() {
  const isLoggedIn = () => !!state.token

  async function login(username, password) {
    state.loading = true
    state.error = null
    try {
      const data = await apiLogin(username, password)
      persist(data.token, { username: data.username, displayName: data.displayName })
      return data
    } catch (e) {
      state.error = e.message
      throw e
    } finally {
      state.loading = false
    }
  }

  async function register(username, password, displayName) {
    state.loading = true
    state.error = null
    try {
      const data = await apiRegister(username, password, displayName)
      persist(data.token, { username: data.username, displayName: data.displayName })
      return data
    } catch (e) {
      state.error = e.message
      throw e
    } finally {
      state.loading = false
    }
  }

  function logout() {
    persist(null, null)
  }

  async function checkStatus() {
    return await fetchAuthStatus()
  }

  function clearError() {
    state.error = null
  }

  return {
    state,
    isLoggedIn,
    login,
    register,
    logout,
    checkStatus,
    clearError,
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/composables/useAuth.js
git commit -m "feat: add useAuth composable for token and login state management"
```

---

### Task 14: 前端 — 创建 LoginView.vue

**Files:**
- Create: `frontend/src/views/LoginView.vue`

- [ ] **Step 1: 创建 LoginView.vue — 左右分栏式登录页**

```vue
<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'

const router = useRouter()
const route = useRoute()
const { login, state, clearError } = useAuth()

const username = ref('')
const password = ref('')
const submitting = ref(false)

const redirect = route.query.redirect || '/dashboard'

async function handleSubmit() {
  if (!username.value.trim() || !password.value.trim()) return
  submitting.value = true
  clearError()
  try {
    await login(username.value.trim(), password.value)
    router.replace(redirect)
  } catch {
    // error is set in state.error by useAuth
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-panel">
      <div class="auth-brand">
        <div class="auth-brand-content">
          <div class="auth-logo">🍽️</div>
          <h2 class="auth-app-name">暖光食刻</h2>
          <p class="auth-tagline">温暖每一餐</p>
        </div>
      </div>

      <div class="auth-form-area">
        <div class="auth-form-card">
          <h3 class="auth-title">管理员登录</h3>

          <form class="auth-form" @submit.prevent="handleSubmit">
            <div class="auth-field">
              <label for="login-username">用户名</label>
              <input
                id="login-username"
                v-model="username"
                type="text"
                autocomplete="username"
                placeholder="请输入用户名"
                :disabled="submitting"
              />
            </div>

            <div class="auth-field">
              <label for="login-password">密码</label>
              <input
                id="login-password"
                v-model="password"
                type="password"
                autocomplete="current-password"
                placeholder="请输入密码"
                :disabled="submitting"
              />
            </div>

            <p v-if="state.error" class="auth-error">{{ state.error }}</p>

            <button
              type="submit"
              class="auth-submit"
              :disabled="submitting || !username.trim() || !password.trim()"
            >
              {{ submitting ? '登录中...' : '登录' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
/* 登录/注册页独立样式 — 不走 App.vue 的 app-shell 布局 */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(223,243,239,0.65), transparent 32%),
              linear-gradient(180deg, #fbfcfc 0%, #f6f8f7 100%);
}

.auth-panel {
  display: flex;
  width: 780px;
  min-height: 460px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 16px 38px rgba(28,44,52,0.08);
}

.auth-brand {
  flex: 1;
  background: linear-gradient(160deg, #168f7f 0%, #0d6b5e 60%, #0a4a3f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-brand-content {
  text-align: center;
  color: #fff;
}

.auth-logo {
  font-size: 48px;
  margin-bottom: 12px;
}

.auth-app-name {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
}

.auth-tagline {
  font-size: 13px;
  opacity: 0.75;
  margin: 0;
}

.auth-form-area {
  flex: 1.2;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
}

.auth-form-card {
  width: 100%;
  max-width: 320px;
}

.auth-title {
  font-size: 18px;
  font-weight: 600;
  color: #202832;
  margin: 0 0 28px 0;
}

.auth-field {
  margin-bottom: 16px;
}

.auth-field label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #202832;
  margin-bottom: 6px;
}

.auth-field input {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #dde5e2;
  border-radius: 8px;
  background: #f9fbfa;
  color: #202832;
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
  box-sizing: border-box;
}

.auth-field input:focus {
  border-color: #168f7f;
  box-shadow: 0 0 0 3px rgba(22,143,127,0.12);
}

.auth-field input:disabled {
  opacity: 0.6;
}

.auth-error {
  color: #d95858;
  font-size: 13px;
  margin: 0 0 16px 0;
}

.auth-submit {
  width: 100%;
  padding: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: #168f7f;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.18s ease, box-shadow 0.18s ease;
  box-shadow: 0 12px 26px rgba(27,138,127,0.22);
}

.auth-submit:hover:not(:disabled) {
  background: #147a6e;
}

.auth-submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.auth-link {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #6d7783;
}

.auth-link a {
  color: #168f7f;
  text-decoration: none;
  font-weight: 500;
}

.auth-link a:hover {
  text-decoration: underline;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/LoginView.vue
git commit -m "feat: add split-panel LoginView"
```

---

### Task 15: 前端 — 创建 RegisterView.vue

**Files:**
- Create: `frontend/src/views/RegisterView.vue`

- [ ] **Step 1: 创建 RegisterView.vue — 左右分栏式注册页**

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth.js'

const router = useRouter()
const { register, checkStatus, state, clearError, isLoggedIn } = useAuth()

const username = ref('')
const password = ref('')
const displayName = ref('')
const submitting = ref(false)
const hasAdmin = ref(false)
const checking = ref(true)

onMounted(async () => {
  if (isLoggedIn()) {
    router.replace('/dashboard')
    return
  }
  try {
    const status = await checkStatus()
    hasAdmin.value = status.hasAdmin
  } catch {
    hasAdmin.value = true // 查询失败时保守处理，禁止注册
  } finally {
    checking.value = false
  }
})

async function handleSubmit() {
  if (!username.value.trim() || !password.value.trim() || !displayName.value.trim()) return
  submitting.value = true
  clearError()
  try {
    await register(username.value.trim(), password.value, displayName.value.trim())
    router.replace('/dashboard')
  } catch {
    // error is set in state.error by useAuth
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-panel">
      <div class="auth-brand">
        <div class="auth-brand-content">
          <div class="auth-logo">🍽️</div>
          <h2 class="auth-app-name">暖光食刻</h2>
          <p class="auth-tagline">温暖每一餐</p>
        </div>
      </div>

      <div class="auth-form-area">
        <div class="auth-form-card">
          <h3 class="auth-title">初始化管理员账号</h3>

          <div v-if="checking" class="auth-status">
            <p>正在检查系统状态...</p>
          </div>

          <div v-else-if="hasAdmin" class="auth-status">
            <p class="auth-error">系统已完成初始化，如需添加账号请联系现有管理员。</p>
          </div>

          <form v-else class="auth-form" @submit.prevent="handleSubmit">
            <div class="auth-field">
              <label for="reg-displayname">显示名称</label>
              <input
                id="reg-displayname"
                v-model="displayName"
                type="text"
                autocomplete="name"
                placeholder="例如：张店长"
                :disabled="submitting"
              />
            </div>

            <div class="auth-field">
              <label for="reg-username">用户名</label>
              <input
                id="reg-username"
                v-model="username"
                type="text"
                autocomplete="username"
                placeholder="3-64位，用于登录"
                :disabled="submitting"
              />
            </div>

            <div class="auth-field">
              <label for="reg-password">密码</label>
              <input
                id="reg-password"
                v-model="password"
                type="password"
                autocomplete="new-password"
                placeholder="至少6位，需包含字母和数字"
                :disabled="submitting"
              />
            </div>

            <p v-if="state.error" class="auth-error">{{ state.error }}</p>

            <button
              type="submit"
              class="auth-submit"
              :disabled="submitting || !username.trim() || !password.trim() || !displayName.trim()"
            >
              {{ submitting ? '创建中...' : '创建管理员账号' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/RegisterView.vue
git commit -m "feat: add split-panel RegisterView with init-only guard"
```

---

### Task 16: 前端 — 更新路由，添加守卫和新路由

**Files:**
- Modify: `frontend/src/router/index.js`

- [ ] **Step 1: 重写 router/index.js 加入守卫和 auth 路由**

将 `frontend/src/router/index.js` 替换为：

```javascript
import { createRouter, createWebHashHistory } from 'vue-router'
import AdminView from '../views/AdminView.vue'
import CustomerView from '../views/CustomerView.vue'
import DashboardView from '../views/DashboardView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import NotFoundView from '../views/NotFoundView.vue'

const PUBLIC_ROUTES = ['/workbench', '/']
const AUTH_ROUTES = ['/login', '/register']

function getToken() {
  return localStorage.getItem('token')
}

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/workbench',
    },
    {
      path: '/workbench',
      name: 'customer',
      component: CustomerView,
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true },
    },
    {
      path: '/products',
      name: 'admin',
      component: AdminView,
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView,
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const hasToken = !!getToken()
  const path = to.path

  if (hasToken && AUTH_ROUTES.includes(path)) {
    return '/dashboard'
  }

  if (!hasToken && to.meta.requiresAuth) {
    return `/login?redirect=${encodeURIComponent(path)}`
  }
})

export default router
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/router/index.js
git commit -m "feat: add auth routes and navigation guards"
```

---

### Task 17: 前端 — AppSidebar 添加登出按钮

**Files:**
- Modify: `frontend/src/components/layout/AppSidebar.vue`

- [ ] **Step 1: 在侧边栏底部添加登出按钮**

修改 `AppSidebar.vue` 的 `<script setup>` 和 `<template>`：

```vue
<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useAuth } from '../../composables/useAuth.js'

defineProps({
  overview: { type: Array, default: () => [] },
})

const router = useRouter()
const { isLoggedIn, logout } = useAuth()

function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-brand">
      <span class="eyebrow">店员工作台</span>
      <h1>暖光食刻</h1>
    </div>

    <nav class="sidebar-nav">
      <RouterLink class="nav-item" to="/workbench" active-class="active"><span>点餐工作台</span></RouterLink>
      <RouterLink class="nav-item" to="/dashboard" active-class="active"><span>数据看板</span></RouterLink>
      <RouterLink class="nav-item" to="/products" active-class="active"><span>商品管理</span></RouterLink>
    </nav>

    <section class="sidebar-overview">
      <div class="sidebar-section-head"><h2>实时概览</h2></div>
      <div class="overview-list">
        <article v-for="item in overview" :key="item.label" class="overview-item">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </div>
    </section>

    <div v-if="isLoggedIn()" class="sidebar-footer">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
  </aside>
</template>
```

- [ ] **Step 2: 在 sidebar 的 CSS 中添加登出按钮样式**

在 `frontend/src/styles/layout.css`（或 sidebar 相关样式文件）末尾添加：

```css
.sidebar-footer {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid var(--border);
}

.logout-btn {
  width: 100%;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 500;
  color: var(--muted);
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.logout-btn:hover {
  color: var(--coral);
  border-color: var(--coral);
  background: var(--coral-soft);
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/layout/AppSidebar.vue frontend/src/styles/layout.css
git commit -m "feat: add logout button to sidebar"
```

---

### Task 18: 前端 — 登录/注册页独立样式处理

**Files:**
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: App.vue 中 auth 页面跳过 sidebar 布局**

修改 `App.vue` 的 `<script setup>`，让 `/login` 和 `/register` 路由不走 `app-shell` 布局：

```vue
<script setup>
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useOrderingStore } from './composables/useOrderingStore'
import AppSidebar from './components/layout/AppSidebar.vue'
import AppToast from './components/layout/AppToast.vue'
import AppLoading from './components/layout/AppLoading.vue'

const store = useOrderingStore()
const route = useRoute()

const isAuthRoute = computed(() =>
  route.path === '/login' || route.path === '/register',
)

const isWorkbenchRoute = computed(() =>
  route.path.startsWith('/workbench') || route.path.startsWith('/customer'),
)

const sidebarOverview = computed(() => [
  { label: '可售商品', value: `${store.safeMenuDishes.length} 道` },
  { label: '今日订单', value: `${store.admin.dashboard.todayOrderCount} 单` },
  { label: '分类数量', value: `${store.manageableCategories.length} 类` },
])

onMounted(() => {
  if (!isAuthRoute.value) {
    store.loadAllData()
  }
})
</script>

<template>
  <div v-if="isAuthRoute" class="auth-shell">
    <RouterView />
  </div>

  <div v-else class="app-shell" :class="{ 'customer-shell': isWorkbenchRoute }">
    <AppSidebar :overview="sidebarOverview" />

    <main class="workspace">
      <AppToast />

      <AppLoading :visible="store.state.loading" />

      <RouterView v-if="!store.state.loading" v-slot="{ Component, route: viewRoute }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" :key="viewRoute.path" />
        </transition>
      </RouterView>
    </main>
  </div>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/App.vue
git commit -m "feat: auth pages skip sidebar layout, use standalone auth-shell"
```

---

### Task 19: 集成验证 — 启动并测试

- [ ] **Step 1: 启动后端**

```bash
cd backend
./.tools/mvnw spring-boot:run
```

Expected: 后端在 `http://localhost:8080` 启动，无报错。

- [ ] **Step 2: 启动前端**

```bash
cd frontend
npm run dev
```

Expected: 前端在 `http://localhost:5173` 启动。

- [ ] **Step 3: 验证注册流程**

1. 浏览器访问 `http://localhost:5173/#/register`
2. 预期看到左右分栏注册页（系统首次初始化，应显示注册表单）
3. 填写：显示名称 "管理员"、用户名 "admin"、密码 "abc123"
4. 点击"创建管理员账号"
5. 预期：自动跳转到 `/dashboard`，sidebar 显示"退出登录"按钮
6. 检查浏览器 localStorage：应有 `token` 和 `user` 键

- [ ] **Step 4: 验证登录流程**

1. 点击 sidebar 底部"退出登录"
2. 预期：跳转到 `/login`
3. 输入用户名 "admin"、密码 "abc123"
4. 点击"登录"
5. 预期：跳转到 `/dashboard`

- [ ] **Step 5: 验证路由守卫**

1. 退出登录后，直接访问 `http://localhost:5173/#/dashboard`
2. 预期：自动重定向到 `/login?redirect=%2Fdashboard`
3. 登录成功后，预期：自动跳转到 `/dashboard`（redirect 参数生效）

- [ ] **Step 6: 验证注册保护**

1. 已存在 admin 的情况下访问 `http://localhost:5173/#/register`
2. 预期：显示"系统已完成初始化"提示，不显示注册表单

- [ ] **Step 7: 验证 API token 注入**

1. 登录后访问 `/products`（商品管理页）
2. 浏览器 DevTools → Network → 查看 `/api/admin/dishes` 请求
3. 预期：请求头包含 `Authorization: Bearer <token>`

- [ ] **Step 8: 验证错误场景**

1. 登录时输入错误密码 → 预期：显示红色错误提示 "用户名或密码错误"
2. 注册时密码为 "123456"（纯数字）→ 预期：后端返回 400 "密码必须包含字母和数字"

- [ ] **Step 9: Commit (如有修正)**

```bash
git add .
git commit -m "chore: integration verification complete"
```

---

### 文件变更汇总

| 操作 | 文件 |
|------|------|
| 新增 SQL | `backend/src/main/resources/schema.sql` (追加) |
| 新增 Java | `backend/.../domain/User.java` |
| 新增 Java | `backend/.../mapper/UserMapper.java` |
| 新增 Java | `backend/.../dto/RegisterRequest.java` |
| 新增 Java | `backend/.../dto/AuthStatusResponse.java` |
| 新增 Java | `backend/.../service/UserService.java` |
| 新增 Java | `backend/.../service/impl/UserServiceImpl.java` |
| 修改 Java | `backend/.../dto/LoginResponse.java` |
| 修改 Java | `backend/.../controller/AuthController.java` |
| 修改 Java | `backend/.../config/SecurityConfig.java` |
| 修改配置 | `backend/.../application.yml` |
| 新增 JS | `frontend/src/api/auth.js` |
| 新增 JS | `frontend/src/composables/useAuth.js` |
| 新增 Vue | `frontend/src/views/LoginView.vue` |
| 新增 Vue | `frontend/src/views/RegisterView.vue` |
| 修改 JS | `frontend/src/router/index.js` |
| 修改 JS | `frontend/src/api/index.js` |
| 修改 Vue | `frontend/src/components/layout/AppSidebar.vue` |
| 修改 Vue | `frontend/src/App.vue` |
| 修改 CSS | `frontend/src/styles/layout.css` |

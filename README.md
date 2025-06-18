# Shop Demo
此demo包含两个模块，买方buyer和卖方seller, 在项目中对应service-buyer和service-seller。
另外抽象出一些公共对象创建公共module common。  

## 本地执行步骤
1. 数据库用mysql,具体配置案例：
````
spring:
  application:
    name: service-buyer
  datasource:
    url: jdbc:mysql://localhost:3306/shop-demo?serverTimezone=UTC
    username: shop
    password: 88888888
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    database-platform: org.hibernate.dialect.MySQLDialect
````
2. 执行数据库建表语句service-buyer/etc/db.sql
3. service-buyer端口 8868, swagger本地地址： http://localhost:8868/swagger-ui/index.html#/
4. service-seller端口 8866, swagger本地地址:  http://localhost:8866/swagger-ui/index.html#/

## service-buyer
1. 先创建用户；
2. 给用户充值，因为要有钱才能购买商品；
3. 下单；
### 实体： 用户，用户订单，用户账户
User, UserOrder, UserAccount
### Rest API:  BuyerController
/buyer/create:  创建新用户  
/buyer/addMoney/{userId}/{money}:  给指定用户的账户充值  
/buyer/order/{userId}/{sku}/{qty}: 下单购买商品，会验证用户是否存在，用户余额是否充足，扣除买方账户需要支付的钱。


## service-seller
买方来购买商品，会把对应商品的库存减少，同时把应付的商品价钱添加到卖方账户。  
因为这里没有账户模块，为了简便，在项目启动前通过sql手动插入一个卖方账户。

### 实体: 商品库存 ProductStore： 
包含原始库存，库存余额，单价. 初始时原始库存和库存余额一样，买方每次下单，从库存余额扣除对应的下单份额。
添加库存的时候，同时把原始库存和库存余额都添加。
### 卖方账户 SellerAccount
买方账户上的钱要和卖出去的商品和其价格的乘积相同。

#### Rest API: SellerController
/seller/createProduct: 创建商品库存，要提供初始库存，单价  
/seller/addStore/{sku}/{qty}:  给已有商品添加库存
/seller/getOrderPrice/{sku}/{qty}:  获取将要下单的总价  
/seller/order/{sku}/{qty}:  提供给service-buyer来做下单，扣库存，给卖方账户添加买方支付的钱

## ScheduleService
用来定时统计商品库存里面卖出去的商品乘以它的单价得到的总价，是否和卖方账户上的钱一致。


# TODO
1. 目前没有加入配置中心和服务注册发现组建，可以根据需要引入nacos来管理分布式集群环境下的服务注册发现以及实时配置管理。
2. 没有引入分布式事务框架，目前虽有根据seller service返回的错误码来回滚事务，但是如果在异步环境下，需要额外的框架来提供分布式事务管理，比如Seata;
3. 定时任务目前加在service-seller这个模块上，但是在集群环境中，应该只需要集群中的某一个seller service去定时执行，那么需要引入分布式锁，比如redis的redission;
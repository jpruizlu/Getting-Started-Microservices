# Authentication and Authorization

## Introduction

Following **security** concepts are essential to **login** into a system:

- **Authentication** means verifying that someone is indeed **who** they claim to be.
- **Authorization** means deciding which **resources** a certain user should be able to access, and what they should be allowed to do with those resources. Usually, an application will require a little bit of both.

![Authentication and Authorization](images/aa.png)

The idea that allows multiple applications to be **authorized** and **authenticated** using a centralized service, was called **single sign-on** (**SSO**). This allows any **user** to enter one username and password in order to access multiple applications. There are multiple solutions for implementing **SSO**. The three most common web security protocols are **OpenID**, **OAuth/2** and **SAML**. Implementations and libraries exist in multiple languages.

![Google OAuth Service](images/google_oauth_login.jpg)

### OAuth/2

**OAuth 1** is an open standard for **access delegation**, commonly used as a way for Internet users to grant websites or applications access to their information on other websites but without giving them the passwords. This mechanism is used by companies such as Amazon, Google, Facebook, Microsoft and Twitter to permit the users to share information about their accounts with third party applications or websites.

![OAuth 1](images/oauth1.png)

**OAuth 2** is an **authorization** framework that enables applications to obtain limited access to user accounts on an HTTP service, such as Facebook, GitHub, and DigitalOcean. It works by delegating user authentication to the service that hosts the user account, and authorizing **third-party** applications to access the user account. OAuth 2 provides authorization flows for web and desktop applications, and mobile devices.

![OAuth 2](images/oauth2.png)

#### OAuth vs OAuth 2

Differences between **OAuth** and **Oauth2**

- Basically, OAuth 2 delegates security to the HTTPS protocol
- **Better** support for **non-browser** based applications. New ways for an application to get authorization for a user.
- **No** longer requires client applications to have **cryptography**.  The application can make a request using only the issued token over HTTPS.
- **Signatures** are much **less** complicated. No more special parsing, sorting, or encoding.
- Access tokens are **short-lived**. Created the notion of **refresh tokens**.
- Clean **separation** of **roles** between the server responsible for handling OAuth requests and the server handling user authorization.

### OpenID Connect

**OpenID 1.0/2.0** are **old** specifications for **authentication**. Those who made the specifications expected people to use OpenID for authentication. However, some people began to use **OAuth 2.0** for authentication (not for authorization) and OAuth authentication has prevailed rapidly. **OAuth2** is also the **basis** for OpenID Connect, which provides OpenID (**authentication**) on top of OAuth2 (**authorization**) for a more complete security solution.

![OpenID Connect](images/oidc-basic-flow.png)

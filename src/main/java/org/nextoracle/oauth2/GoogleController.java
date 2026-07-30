package org.nextoracle.oauth2;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
public class GoogleController {

    @GetMapping(value = "/google.html", produces = MediaType.TEXT_HTML_VALUE)
    public String googlePage() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Google OAuth2</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #111; font-family: -apple-system, sans-serif; color: #eee; }
                        .container { text-align: center; max-width: 600px; }
                        a.google-btn {
                            display: inline-flex; align-items: center; gap: 12px;
                            background: #fff; color: #444; text-decoration: none;
                            padding: 12px 24px; border-radius: 4px; font-size: 15px; font-weight: 500;
                            box-shadow: 0 2px 4px rgba(0,0,0,.25);
                            transition: box-shadow .2s;
                        }
                        a.google-btn:hover { box-shadow: 0 4px 8px rgba(0,0,0,.3); }
                        a.google-btn img { width: 20px; height: 20px; }
                        .result { background: #1a1a1a; padding: 20px; border-radius: 8px; margin-top: 24px; text-align: left; word-break: break-all; font-size: 13px; }
                        .success { color: #4caf50; }
                        .token { color: #90caf9; font-family: monospace; font-size: 11px; margin-top: 8px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div id="login">
                            <a class="google-btn" href="/oauth2/authorization/google">
                                <img src="https://developers.google.com/identity/images/g-logo.png" alt="G"/>
                                Sign in with Google
                            </a>
                        </div>
                        <div id="result"></div>
                    </div>
                    <script>
                        const params = new URLSearchParams(window.location.search);
                        const token = params.get('token');
                        if (token) {
                            document.getElementById('login').style.display = 'none';
                            const payload = JSON.parse(atob(token.split('.')[1]));
                            document.getElementById('result').innerHTML = `
                                <div class="result">
                                    <p class="success">✅ Login successful!</p>
                                    <p style="margin-top:8px"><strong>User:</strong> ${payload.sub}</p>
                                    <p><strong>Roles:</strong> ${payload.roles}</p>
                                    <p><strong>Expires:</strong> ${new Date(payload.exp * 1000).toLocaleString()}</p>
                                    <p class="token"><strong>JWT:</strong> ${token}</p>
                                </div>
                            `;
                        }
                    </script>
                </body>
                </html>
                """;
    }
}


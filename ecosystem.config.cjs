module.exports = {
  apps: [
    {
      name: "promethean-agent-system-demo",
      cwd: __dirname,
      script: "clojure",
      args: ["-M", "-e", "(require 'promethean.demo) (promethean.demo/run!)"],
      interpreter: "none",
      out_file: "logs/pm2/demo.out.log",
      error_file: "logs/pm2/demo.err.log",
      log_date_format: "YYYY-MM-DD HH:mm:ss",
      autorestart: false,
    },
  ],
};

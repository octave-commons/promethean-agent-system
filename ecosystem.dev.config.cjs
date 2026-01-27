module.exports = {
  apps: [
    {
      name: "promethean-agent-system-dev",
      cwd: __dirname,
      script: "clojure",
      args: ["-M", "-e", "(require 'promethean.demo) (promethean.demo/run!)"],
      interpreter: "none",
      watch: ["src", "deps.edn"],
      ignore_watch: [
        "node_modules",
        "logs",
        ".clj-kondo",
        ".cpcache",
        "target",
        "classes",
      ],
      watch_delay: 1000,
      out_file: "logs/pm2/dev.out.log",
      error_file: "logs/pm2/dev.err.log",
      log_date_format: "YYYY-MM-DD HH:mm:ss",
      autorestart: true,
    },
  ],
};

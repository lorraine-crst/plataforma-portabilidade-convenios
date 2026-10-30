import { AppBar, Container, Toolbar, Typography } from "@mui/material";
import { Link, Outlet } from "react-router";

export function Layout() {
  return (
    <>
      <AppBar position="static" color="secondary" elevation={0}>
        <Toolbar>
          <Typography
            component={Link}
            to="/"
            variant="h6"
            sx={{ color: "inherit", fontWeight: 700, textDecoration: "none" }}
          >
            Nuvola
          </Typography>
        </Toolbar>
      </AppBar>
      <Container component="main" maxWidth="lg" sx={{ py: 3 }}>
        <Outlet />
      </Container>
    </>
  );
}

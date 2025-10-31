@echo off
git add .
git commit -m "Auto-commit: Updated files on %date% %time%"
git push origin master
echo Changes pushed to GitHub successfully!
pause
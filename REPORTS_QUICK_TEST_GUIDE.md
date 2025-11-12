# Reports Module - Quick Test Guide

## How to Test Report Generation & PDF Sharing

### Step 1: Access Reports Screen
1. Open the app
2. Navigate to **Home** screen
3. Tap on **Reports** menu option
   - OR tap on **More** tab → **Reports**

### Step 2: Select a Report Type
You'll see a grid of 17 report types:
- Sale Report
- Purchase Report
- Expense Report
- Extra Income Report
- Top Products
- Top Customers
- Stock Report
- Product Report
- Customer Report
- Vendor Report
- Profit & Loss Report
- Cash in Hand
- Balance Report
- Profit & Loss by Purchase
- Balance Sheet
- Investor Report
- Warehouse Report

Tap any report type to continue.

### Step 3: Configure Filters
On the Report Filters screen:
1. **Additional Filter**: Select one (Overall, Daily, Weekly, Monthly, Yearly)
2. **Date Filter**: Choose duration (Today, Yesterday, Last 7 days, This month, etc.)
3. **Group By** (optional): Product, Party, Category, etc.
4. **Sort By**: Title, Profit, or Amount (ascending/descending)
5. Tap **Generate Report** button

### Step 4: View Report
The report will load and display:
- **Summary Card**: Shows totals, profit, quantity, record count
- **Filter Info Card**: Shows applied filters
- **Data Table**: Headers and rows with report data
- Sample data is pre-generated for demo purposes

### Step 5: Share as PDF
1. Tap the **Share** icon (↗️) in the top-right app bar
2. Wait for "Generating PDF..." overlay (1-2 seconds)
3. System share dialog will appear
4. Select where to share:
   - Gmail/Email
   - WhatsApp/Messaging
   - Google Drive/Cloud Storage
   - Any other sharing app

### Expected Results

#### ✅ Report Display
- Clean, professional table layout
- Summary metrics at the top
- Filter information displayed
- Scrollable data rows
- Material Design 3 styling

#### ✅ PDF Generation
- PDF file created in app cache directory
- File name format: `Report_[ReportType]_[Timestamp].pdf`
- A4 size PDF with proper formatting
- Contains: Title, timestamp, summary, filters, data table
- First 25 rows shown with count indicator

#### ✅ Share Functionality
- System share dialog opens
- PDF can be sent via email, messaging, cloud storage
- File permissions granted automatically via FileProvider

### Sample Data Examples

**Sale Report**
```
Date        Invoice #   Customer       Amount      Profit
2024-01-15  INV-001    John Doe       Rs 15,000   Rs 3,000
2024-01-16  INV-002    Jane Smith     Rs 25,000   Rs 5,500
...
Summary: Total Rs 99,000 | Profit Rs 21,300 | 5 records
```

**Top Products Report**
```
Rank  Product    Quantity  Revenue     Profit
#1    Product A  150       Rs 75,000   Rs 15,000
#2    Product B  120       Rs 60,000   Rs 13,500
...
Summary: Total Rs 265,000 | Profit Rs 58,000 | 530 units
```

**Balance Report**
```
Party          Type      Total Sales  Total Paid   Balance
John Doe       Customer  Rs 100,000   Rs 80,000    Rs 20,000
ABC Suppliers  Vendor    Rs 150,000   Rs 100,000   Rs -50,000
...
Summary: Net Balance Rs -30,000 | 3 records
```

### Testing Different Scenarios

#### Test 1: Different Report Types
- Try each of the 17 report types
- Verify data structure is appropriate for each type
- Check summary cards show relevant metrics

#### Test 2: Filter Variations
- Change date filter (Today, Last 7 days, This month, etc.)
- Try different grouping options
- Test different sort orders
- Verify filter info displays correctly

#### Test 3: PDF Sharing
- Generate PDF from different report types
- Share to different apps (email, WhatsApp, Drive)
- Verify PDF opens correctly in viewer apps
- Check PDF formatting and content

#### Test 4: Navigation
- Back button from each screen
- Switching between report types
- Generating multiple reports in sequence
- Verify state is properly cleared

#### Test 5: Edge Cases
- Very long report names
- Empty report results (future when connected to DB)
- Multiple rapid PDF generations
- Sharing cancellation

## Troubleshooting

### Issue: PDF Share Dialog Doesn't Open
**Solution**: Check AndroidManifest.xml has FileProvider configured and file_paths.xml includes cache-path

### Issue: "Generating PDF..." Hangs
**Solution**: Check logcat for errors. Ensure Android Context is properly injected via Koin

### Issue: Empty Report Display
**Solution**: Normal - sample data is pre-generated. Will show actual data when connected to database

### Issue: Back Navigation Doesn't Work
**Solution**: Ensure App.kt properly handles REPORT_RESULT in navigation stack

## Next Steps

After testing with sample data:
1. Connect GenerateReportUseCase to actual database queries
2. Apply user-selected filters to database queries
3. Add real-time data fetching
4. Implement date range filtering logic
5. Add export to Excel/CSV
6. Implement iOS PDF generation
7. Add charts and visualizations

## Notes

- All 17 report types currently use **sample/mock data**
- PDF generation is **Android-only** (iOS stub in place)
- Share functionality is **Android-only** (iOS stub in place)
- Reports are saved to **app cache** (auto-cleaned by system)
- FileProvider ensures **secure file sharing**
- No external storage permissions required

## Validation Checklist

- [ ] All 17 report types are selectable
- [ ] Filter configuration works correctly
- [ ] "Generate Report" button is clickable
- [ ] Loading indicator appears
- [ ] Report displays with correct layout
- [ ] Summary card shows metrics
- [ ] Filter info card displays correctly
- [ ] Data table has headers and rows
- [ ] Share icon is visible in app bar
- [ ] Tapping share generates PDF
- [ ] "Generating PDF..." overlay appears
- [ ] System share dialog opens
- [ ] PDF can be shared to other apps
- [ ] PDF opens correctly in viewer
- [ ] PDF contains all expected sections
- [ ] Back navigation works correctly
- [ ] No crashes or errors
- [ ] UI is responsive and smooth

**Test Status**: ✅ Ready for testing with sample data
**Production Ready**: ⚠️ Requires database integration for actual data

